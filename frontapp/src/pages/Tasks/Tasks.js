import { Heading } from "grommet";
import { Link } from "react-router-dom";

import ContestNav from "../../common/ContestNav";
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
  return (
    <>
      <ContestNav current="tasks" />
      <Heading>Contest tasks:</Heading>
      <ContestTaskTable rows={rows} />
    </>
  );
}
