import { Paragraph, Select } from "grommet";
import { useCallback, useState } from "react";

import ContestNav from "../../common/ContestNav";
import PersonalSubmissionsTable from "./PersonalSubmissionsTable";
import SubmissionModal from "./SubmissionModal";

export default function personalSubmissions() {
  const [task, setTask] = useState();
  const [showSubmission, setShowSubmission] = useState(false);

  const onClose = useCallback(()=> {setShowSubmission(false)}, [])

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
        <button onClick={()=>setShowSubmission(true)}></button>
      </div>
      <SubmissionModal show={showSubmission} code={`keys_list = ['A', 'B', 'C']
values_list = ['blue', 'red', 'bold']

#There are 3 ways to convert these two lists into a dictionary
#1- Using Python's zip, dict functionz
dict_method_1 = dict(zip(keys_list, values_list))

#2- Using the zip function with dictionary comprehensions
dict_method_2 = {key:value for key, value in zip(keys_list, values_list)}

#3- Using the zip function with a loop
items_tuples = zip(keys_list, values_list) 
dict_method_3 = {} 
for key, value in items_tuples: 
    if key in dict_method_3: 
        pass # To avoid repeating keys.
    else: 
        dict_method_3[key] = value`} onClose={onClose}/>
      <PersonalSubmissionsTable/>
    </>
  );
}
