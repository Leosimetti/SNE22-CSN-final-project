import { Paragraph, Select } from "grommet";
import { useCallback, useContext, useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";

import ContestNav from "../../common/ContestNav";
import ProtoContext from "../../common/ProtoContext";
import useLocalStorage from "../../common/useLocalStorage";
import PersonalSubmissionsTable from "./PersonalSubmissionsTable";
import SubmissionModal from "./SubmissionModal";

export default function personalSubmissions() {
  const [task, setTask] = useState();
  const [showSubmission, setShowSubmission] = useState(false);
  const proto = useContext(ProtoContext);
  const [problems] = useLocalStorage("Problems", []);
  const [shared, client] = [proto.shared, proto.client];
  const submissions = useRef([]);
  const [code, setCode] = useState();
  const [tableData, setTableData] = useState([]);

  function normalizeData(entries) {
    return entries.map((el) =>
      createRow(
        el.type,
        el.duration,
        <Link onClick={()=>handleOpenWindow(el.code)}>Review code</Link>
      )
    );
  }

  function handleSelect(task) {
    console.log(submissions)
    setTask(task);
    let entries = submissions.current;
    // entries = entries.filter((el) => el.task === task);
    console.log(entries);
    setTableData(normalizeData(entries));
  }

  function handleOpenWindow(code) {
    setCode(code);
    setShowSubmission(true);
  }

  function createRow(result, duration, solution) {
    return { result, duration, solution };
  }

  useEffect(() => {
    var req = new shared.MySubmissionsRequest();
    req.setUserid("aboba");

    var stream = client.mySubmissions(req, {});

    stream.on("data", function (response) {
      const entry = {};
      response = response.getResult();

      if (response.getSuccess()) {
        entry.type = "Success";
        response = response.getSuccess();
      } else {
        entry.type = "Failure";
        response = response.getFailure();
      }
      entry.task = response.getTaskid();
      entry.solution = response.getSolution();
      entry.duration = response.getDuration();
      entry.code = entry.solution.getCode();
      submissions.current.push(entry);
    });
  }, []);

  const onClose = useCallback(() => {
    setShowSubmission(false);
  }, []);

  return (
    <>
      <ContestNav />
      <div className="selectFormFieldContainer">
        <div className="selectFormFieldName">
          <Paragraph responsive style={{ maxWidth: "100%" }} size="xlarge">
            Task:
          </Paragraph>
        </div>
        <Select
          id="select_task"
          name="select_task"
          placeholder="Select task:"
          value={task}
          options={problems.map((el) => el.name)}
          onChange={({ option }) => handleSelect(option)}
          style={{ width: "300px" }}
        />
        <button onClick={() => setShowSubmission(true)}></button>
      </div>
      <SubmissionModal
        show={showSubmission}
        language="python"
        code={code}
        onClose={onClose}
      />
      <PersonalSubmissionsTable rows={tableData} />
    </>
  );
}
