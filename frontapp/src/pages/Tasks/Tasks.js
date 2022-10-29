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
        10.10.2022: <br /> 10.10.2023
      </>
    ),
    solved: "0",
    action: (
      <Link to={URLs.taskDescription + "?name=" + name}>{"Check task"}</Link>
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
          LSsetProblems(
            problems.map((el) => {
              return { name: el.array[0], description: el.array[1] };
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
