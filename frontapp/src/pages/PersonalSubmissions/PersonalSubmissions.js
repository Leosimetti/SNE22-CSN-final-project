import { Paragraph, Select } from "grommet";
import { useState } from "react";

import ContestNav from "../../common/ContestNav";
import PersonalSubmissionsTable from "./PersonalSubmissionsTable";

export default function personalSubmissions() {
  const [task, setTask] = useState();

  const TASK_OPTIONS = [
    "A-Josko posrat",
    "B-Jidko nasrat",
    "C-Silno obosratsa",
  ];

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
          options={TASK_OPTIONS}
          onChange={({ option }) => setTask(option)}
          style={{ width: "300px" }}
        />
      </div>
      <PersonalSubmissionsTable></PersonalSubmissionsTable>
    </>
  );
}
