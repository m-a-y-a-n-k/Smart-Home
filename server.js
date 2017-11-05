/* SENDS DATA TO REAL TIME DATABASE */

var admin = require("firebase-admin");
var serviceAccount = require("./serviceAccountKey.json");
var chance = require('chance').Chance();
var express = require('express');
var app = express();
const threads = require('threads');
const spawn   = threads.spawn;
const thread  = spawn(function() {});
 
admin.initializeApp({
	credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://isecpowify.firebaseio.com/",
  databaseAuthVariableOverride: {
    uid: "my-service-worker"
  }
});

// authenticate with required privileges
var db = admin.database();
var stateRef = db.ref("/state");
var usersRef = db.ref("/users");


// store 5 random users in database untill there are 25 users in total
function addUsers(){
	var userCount = 0,pushCount = 0;
	usersRef.on("value",function(snap){
		userCount = snap.numChildren();
		while( userCount <= 20 && pushCount < 5 ){
			var gen = chance.gender();
			chance.mixin({
    			'user': function() {
        					return {
					            name : chance.name(
					            		{ 	middle: true, 
					            			prefix : true, 
					            			gender : gen 
					            		}),
					            gender : gen,
					            dob: chance.birthday({string: true, american: false}),
					            email: chance.email({domain: 'gmail.com'}),
					            ssn : chance.ssn(),
					            profession : chance.profession(),
					            contact : chance.phone({ formatted: false, mobile: true})
        					};
    					}
			});
			usersRef.push().set( chance.user());
			pushCount++;
			userCount = snap.numChildren();
		}
	}, function (errorObject) {
  		console.log("The read failed: " + errorObject.code);
	});
	
}

// store data packets of sensor readings for 45 minutes in database maintaining 10 minutes window of state each second
function updateStateRecordWindow(){

	var Now = Date.now();
	var Limit = Now + 45*60*1000;

	thread
	  .run(function(limit, done, progress) {
	  	var Now = Date.now();

		function sleep(time) {
    		var stop = Date.now();
    		while(Date.now() < stop + time) {
        		;
    		}
		}
	    setTimeout(done, limit - Now);					// 45 minutes time limit
	    while(Now <= limit){
	    	sleep(1000);								// publish progress after 1 second
	    	Now = Date.now();
	    	progress(Now);
	    }
	  })
	  .send(Limit)
	  .on('progress', function(Now) {
	  				var Old = Now - 10*60*1000; 			// 10 minutes older data remove
					stateRef.orderByChild('timestamp').endAt(Old).once('value', function(snapshot) {
    					snapshot.forEach(function(snap){
    						stateRef.child(snap.val().timestamp).remove();
    					});
					});
					var lim,lom,gx,gy,gz,gs,im,om;
					chance.mixin({
			    		'state': function() {

			    				im = chance.bool({likelihood: 40});
			    				om = chance.bool({likelihood: 60}); 
			    				if( im == true ){
			    					lim = Now;
			    				}
			    				if( om == true ){
			    					lom = Now;
			    				}
			    				if( lim == null || lim == undefined ){
			    					lim = 0;
			    				}
			    				if( lom == null || lom == undefined ){
			    					lom = 0;
			    				}
			    				do{
			    					gx = chance.floating({min: 0, max: 8, fixed : 3});
			    					gy = chance.floating({min: 0, max: 8, fixed : 3});	
			    					gz = chance.floating({min: 0, max: 8, fixed : 3});	
			    					gs = gx*gx + gy*gy + gz*gz;
			    				}while( gs <= 80 || gs >= 144 );
				    			
				    			return {
								            light : chance.natural({min: 150, max: 500}),
								            temp : chance.floating({min: 250, max: 750, fixed : 4}),
								            humidity : chance.floating({min: 0, max: 100, fixed : 2}) ,
								            inDoorMotion : im,
								            outDoorMotion : om,
								            ax : gx,
								            ay : gy,
								            az : gz,
								            latestIndoorMovement : lim,
								            latestOutdoorMovement : lom,
								            timestamp : Now
								            
			        			};
			    			}
					});
					stateRef.child(Now).set(chance.state());
	  })
	  .on('done', function() {
	    thread.kill();
	  });
}

function pushSensorData(t,h,l,im,om,gx,gy,gz){

	var Now = Date.now();

	var state = {
		light : l || null,
		temp : t || null,
		humidity : h || null,
		inDoorMotion : im || null ,
		outDoorMotion : om || null ,
		ax : gx || null ,
		ay : gy || null,
		az : gz || null ,
		latestIndoorMovement : 0,
		latestOutdoorMovement : 0,
		timestamp : Now
	};

	var Old = Now - 10*60*1000; 			// remove all data older than 10 minutes
	
	stateRef.orderByChild('timestamp').endAt(Old).once('value', function(snapshot) {
    	snapshot.forEach(function(snap){
    		stateRef.child(snap.val().timestamp).remove();
    	});
	});
	stateRef.child(Now).set(state);
}

function isEmpty(obj) {
  return !Object.keys(obj).length > 0;
}

app.get("/u/users",function(req,response){
    response.writeHead(200, {'Content-type':'text/plain'});
  	response.write('Smart Home IoT Admin created Users\n');
   	response.end();
   	addUsers();
});

app.get("/u/state",function(req,response){
    response.writeHead(200, {'Content-type':'text/plain'});
  	response.write('Smart Home IoT Sensor Data push endpoint\n');
   	response.end();
   	var q = req.query;

   	stateRef.on("child_added",function(snapshot,prevChildKey){
		var newState = snapshot.val();
		var prevState;
		if( prevChildKey != null )
		{
			stateRef.child(prevChildKey).on("value",function(snap){
				prevState = snap.val();
				if( prevState != null && prevState != undefined ){
					var lim,lom,ts;
					lim = newState.latestIndoorMovement;
					lom = newState.latestOutdoorMovement;
					ts = newState.timestamp;
					if( lim == 0)
					{
						var changes = {
							latestIndoorMovement : prevState.latestIndoorMovement
						}
						if( ts != undefined && ts != null )
						stateRef.child(""+ts).update(changes);		
					}
					if( lom == 0 ){
				
						var changes = {
							latestOutdoorMovement : prevState.latestOutdoorMovement
						}
						if( ts != undefined && ts != null )
							stateRef.child(""+ts).update(changes);	
					}
				}	
			});
		}
	});



   	if( isEmpty(q) ){

   		// acts as direct continous uploading server
   		updateStateRecordWindow();	
   	}
   	else {

   		// indirectly uploads values to cloud from recieved endpoint
   		pushSensorData(q.temp,q.humidity,q.light,q.inDoorMotion,q.outDoorMotion,q.ax,q.ay,q.az);
   	}
});
app.listen(2300);
