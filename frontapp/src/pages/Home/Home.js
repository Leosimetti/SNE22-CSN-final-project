import { Box, Header, Heading } from "grommet";
import "./Home.css";

import ContestTable from "./ContestTable";
import { Link } from "react-router-dom"

function createData(name, start, duration, action) {
  return { name, start, duration, action };
}

const rows = [
  createData("Contest name: Aboba", <>12.12.2012<br/>25:22</>, "2h", <Link to="/tasks">Aboba</Link>),
  createData("Contest name: Booba", "12.12.2012 25:22", "2h", "Go to task"),
];

export default function Home() {
  return (
    <div className="homeContainer">
      <Heading>Upcoming and current contests:</Heading>
      <ContestTable rows={rows}/>
      <Heading>Finished contests:</Heading>
      <ContestTable rows={rows}/>
    </div>
  );
}
