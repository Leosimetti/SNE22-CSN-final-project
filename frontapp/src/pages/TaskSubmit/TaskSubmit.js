import "./TaskSubmit.css";

import CodeEditor from "@uiw/react-textarea-code-editor";
import { Button , Form,Heading , Paragraph , Select  } from "grommet";
import { useRef, useState } from "react";

import ContestNav from "../../common/ContestNav";
import { APPLICATION_SERVER } from "../../common/URLs";

function toTitleCase(str) {
  return str.replace(/\w\S*/g, function (txt) {
    return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
  });
}

export default function TaskSubmit() {
  const TASK_OPTIONS = [
    "A-Josko posrat",
    "B-Jidko nasrat",
    "C-Silno obosratsa",
  ];
  const PL_OPTIONS = ["cpp", "python", "scala", "java"];

  const [task, setTask] = useState("");
  const [language, setLanguage] = useState("");

  const codeRef = useRef();

  async function onSubmit() {
    // const PROBLEM_ID = "1";
    const USER_ID = "1";

    const data = {
      problemId: task,
      userId: USER_ID,
      solution: {
        language: toTitleCase(language),
        code: codeRef.current.value,
      },
    };

    console.log(data);

    const response = await fetch(APPLICATION_SERVER, {
      method: "POST",
      mode: "no-cors", //TODO: CORS
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data),
    }).catch((err) => console.log(err));
    const responseJSON = await response.text();

    console.log(responseJSON);
  }

  return (
    <>
      <ContestNav />

      <Heading>Contest name: ABOBA</Heading>

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
            options={TASK_OPTIONS}
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
