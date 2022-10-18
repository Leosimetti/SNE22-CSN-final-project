import Button from "@mui/material/Button";
import ButtonGroup from "@mui/material/ButtonGroup";
import { useNavigate } from "react-router-dom";
import { useLocation } from "react-router-dom";

import URLs from "./URLs";

export default function ContestNav() {
  const navigate = useNavigate();
  const currentLocation = useLocation();

  function goTo(link) {
    navigate(link);
  }

  return (
    <ButtonGroup
      variant="outlined"
      aria-label="outlined button group"
      size="large"
      color="warning"
    >
      <Button disabled={currentLocation.pathname === URLs.tasks} onClick={() => goTo(URLs.tasks)}>
        Tasks
      </Button>
      <Button disabled={currentLocation.pathname === URLs.taskSubmit} onClick={() => goTo(URLs.taskSubmit)}>
        Submit solution
      </Button>
      <Button disabled={currentLocation.pathname === URLs.personalSubmissions} onClick={() => goTo(URLs.personalSubmissions)}>
        My submissions
      </Button>
    </ButtonGroup>
  );
}
