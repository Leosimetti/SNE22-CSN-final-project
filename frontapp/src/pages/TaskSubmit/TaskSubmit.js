import "./TaskSubmit.css";

import CodeEditor from "@uiw/react-textarea-code-editor";
import { Button, Form, Heading, Paragraph, Select } from "grommet";
import { useContext, useRef, useState } from "react";

import ContestNav from "../../common/ContestNav";
import ProtoContext from "../../common/ProtoContext";
import useLocalStorage from "../../common/useLocalStorage";
import { PL_OPTIONS } from "./TaskSubmitHelper.js";

export default function TaskSubmit() {
  const [task, setTask] = useState("");
  const [language, setLanguage] = useState("");
  const proto = useContext(ProtoContext);
  const [problems] = useLocalStorage("Problems", []);

  const codeRef = useRef();

  async function onSubmit() {
    const [shared, client] = [proto.shared, proto.client];

    const userSubmission = new shared.UserSubmission();
    const solution = new shared.Solution();

    solution.setCode(codeRef.current.value);
    solution.setLanguage(shared.Language.PYTHON);

    userSubmission.setProblemid(task);
    userSubmission.setUserid("aboba");
    userSubmission.setSolution(solution);

    client.submit(userSubmission, null, function (err, response) {
      console.log(response);
      console.log(err);
    });
  }

  return (
    <>
      <ContestNav />

      <Heading>Contest name: Cup of the year</Heading>

      <Form onSubmit={() => onSubmit()}>
        <div className="selectFormFieldContainer">
          <div className="selectFormFieldName">
            <Paragraph
              id="select_task"
              responsive
              style={{ maxWidth: "100%" }}
              size="xlarge"
            >
              Task:
            </Paragraph>
          </div>
          <Select
            id="select_task"
            name="select_task"
            placeholder="Select task"
            value={task}
            options={problems.map((el)=>el.name)}
            onChange={({ option }) => setTask(option)}
            style={{ width: "300px" }}
          />
        </div>

        <div className="selectFormFieldContainer">
          <div className="selectFormFieldName">
            <Paragraph responsive style={{ maxWidth: "100%" }} size="xlarge">
              Programming language:
            </Paragraph>
          </div>
          <Select
            id="select_pl"
            name="select_pl"
            placeholder="Select programming language"
            value={language}
            options={PL_OPTIONS}
            onChange={({ option }) => setLanguage(option)}
            style={{ width: "300px" }}
          />
        </div>

        <Paragraph responsive style={{ maxWidth: "100%" }} size="xlarge">
          Enter code:
        </Paragraph>
        <div className="codeEditorContainer">
          <div data-color-mode="light">
            <CodeEditor
              value={""}
              language={language}
              placeholder="Please enter code. (Syntax highlighting is active only for specified programming language.)"
              padding={15}
              ref={codeRef}
              style={{
                fontSize: 20,
                fontFamily:
                  "ui-monospace,SFMono-Regular,SF Mono,Consolas,Liberation Mono,Menlo,monospace",
                background: "#f5f5f5",
                minHeight: "150px",
              }}
            />
          </div>
        </div>

        <Button
          label="Submit"
          size="large"
          type="submit"
          className="submitButton"
          style={{ margin: "20px 0" }}
        />
      </Form>
    </>
  );
}
