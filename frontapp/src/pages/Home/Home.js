import "./Home.css";

import { Heading } from "grommet";
import { Link } from "react-router-dom";

import ContestTable from "./ContestTable";

function createData(name, start, duration, action) {
  return { name, start, duration, action };
}

const rows1 = [
  createData(
    "Contest name: Cup of the year",
    <>
      1.1.2022
    </>,
    "1 year",
    <Link to="/tasks">Go to tasks</Link>
  ),
  createData("Contest name: Temp contest", "12.12.2022", "1 day", "Not started"),
];

const rows2 = [
  createData(
    "Contest name: Cup of the previous year",
    <>
      1.1.2021
    </>,
    "1 year",
    <Link to="/tasks">Go to tasks</Link>
  )
];

export default function Home() {
  return (
    <div className="homeContainer">
      <Heading>Upcoming and current contests:</Heading>
      <ContestTable rows={rows1} />
      <Heading>Finished contests:</Heading>
      <ContestTable rows={rows2} />
    </div>
  );
}
