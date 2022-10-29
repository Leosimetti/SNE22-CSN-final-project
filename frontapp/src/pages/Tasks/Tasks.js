import { Heading } from "grommet";
import { useContext, useEffect, useState } from "react";
import { Link } from "react-router-dom";

import ContestNav from "../../common/ContestNav";
import ProtoContext from "../../common/ProtoContext";
import URLs from "../../common/URLs";
import useLocalStorage from "../../common/useLocalStorage";
import ContestTaskTable from "./ContestTasksTable";

function createData(name) {
  return {
    name,
    status: (
      <>
        01.01.2022: <br /> 01.01.2023
      </>
    ),
    solved: "0",
    action: (
      <Link
        to={
          URLs.taskDescription + "?" + (new URLSearchParams({ name }).toString())
        }
      >
        {"Check task"}
      </Link>
    ),
  };
}

export default function Tasks() {
  const problemIds = new Set();
  const [problems, setProblems] = useState([]);

  const proto = useContext(ProtoContext);
  const [shared, client] = [proto.shared, proto.client];
  const [LSProblems, LSsetProblems] = useLocalStorage("Problems", null);

  useEffect(() => {
    if (LSProblems) {
      setProblems(LSProblems.map((el) => createData(el.name)));
    } else {
      var req = new shared.Empty();
      client.getProblems(req, null, (err, res) => {
        if (err) {
          console.log(err);
        } else {
          let rows = [];
          const problems = res.getProblemsList();
          for (const i of problems) {
            const problemdata = i.array;
            problemIds.add(problemdata[0]);
          }
          rows = [...problemIds].map((el) => createData(el));
          console.log(problems[0].getExamplesList()[0]);
          LSsetProblems(
            problems.map((el) => {
              return {
                name: el.getProblemid(),
                description: el.getProblemtext(),
                examples: el.getExamplesList().map((el) => el.array),
                inputDescription: el.getInputdescription(),
                outputDescription: el.getOutputdescription(),
              };
            })
          );
          setProblems([...rows]);
        }
      });
    }
  }, []);

  return (
    <>
      <ContestNav current="tasks" />
      <Heading>Contest tasks:</Heading>
      <ContestTaskTable rows={problems} />
    </>
  );
}
