
import './App.css';
import React, { useState, useEffect } from 'react';
import { AppClient } from './schema_grpc_web_pb';
// import * as aboba from './schema_pb'
// import { UserSubmission, Solution, Language } from './schema_pb';

require('./schema_pb.js')
var shared = require('./schema_grpc_web_pb.js')

// We create a client that connects to the api
var client = new AppClient("http://localhost:10000");

function App() {
  // Create a const named status and a function called setStatus
  const [status, setStatus] = useState(false);
  // sendPing is a function that will send a ping to th backend

  const sendPing = () => {

    var req = new shared.MySubmissionsRequest();
    req.setUserid("aboba")

    var stream = client.mySubmissions(req, {});

    stream.on('data', function (response) {
      console.log(response);
      setStatus(response.getResult())
    });
    stream.on('status', function (status) {
      console.log("status: ", status.code, status.details, status.metadata);

    });
    stream.on('error', function (end) {
      console.log("err: "+end)
    });
    stream.on('metadata', function (end) {
      console.log("meta: "+end)
    });
    stream.on('end', function (end) {
      console.log(end)
    });


    // stream.cancel()

    console.log(stream);


    // var userSubmission = new shared.UserSubmission();
    // var solution = new shared.Solution();
    // solution.setCode("print(a+b)");
    // solution.setLanguage(shared.Language.PYTHON);
    // // console.log(solution)
    // userSubmission.setProblemid("a+b");
    // userSubmission.setUserid("aboba");
    // userSubmission.setSolution(solution);

    // // use the client to send our pingrequest, the function that is passed
    // // as the third param is a callback. 
    // client.submit(userSubmission, null, function (err, response) {

    //   console.log(response)
    //   console.log(err)
    //   // serialize the response to an object 
    //   // var pong = response.toObject();
    //   // call setStatus to change the value of status
    //   // setStatus(pong.ok);
    // });
  }

  useEffect(() => {
    // Start a interval each 3 seconds which calls sendPing.

    // const interval = setInterval(() => sendPing(), 5000)
    return () => {
      // reset timer
      sendPing()
      // clearInterval(interval);
    }
  }, [status]);

  // we will return the HTML. Since status is a bool
  // we need to + '' to convert it into a string
  return (
    <div className="App">
      <p>Status: {status + ''}</p>
    </div>
  );


}


export default App;