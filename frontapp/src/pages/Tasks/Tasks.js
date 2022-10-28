import { Heading } from "grommet";
import { useContext, useEffect } from "react";
import { Link } from "react-router-dom";

import ContestNav from "../../common/ContestNav";
import ProtoContext from "../../common/ProtoContext";
import ContestTaskTable from "./ContestTasksTable";

function createData(name, status, solved, action) {
  return { name, status, solved, action };
}

const rows = [
  createData(
    "Contest name: Aboba",
    <>
      12.12.2012
      <br />
      25:22
    </>,
    "2h",
    <Link to="/task">Go to task</Link>
  ),
  createData("Contest name: Booba", "12.12.2012 25:22", "2h", "Go to task"),
];

export default function Tasks() {
  const proto = useContext(ProtoContext);
  const [shared, client] = [proto.shared, proto.client];

  useEffect(() => {
    var req = new shared.Empty();
    client.getProblems(req, null, (a, b) => {
      console.log(a, b);
    });
  });

  return (
    <>
      <ContestNav current="tasks" />
      <Heading>Contest tasks:</Heading>
      <ContestTaskTable rows={rows} />
    </>
  );
}
