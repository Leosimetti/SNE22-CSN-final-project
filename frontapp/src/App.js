import {
  Grommet,
  Box,
  Grid,
  Header,
  Heading,
  Page,
  PageContent,
} from "grommet";
import { BrowserRouter as Router, Routes, Route, Link } from "react-router-dom";
import Home from "./pages/Home/Home";
import TaskDescription from "./pages/TaskDescription/TaskDescription";
import Tasks from "./pages/Tasks/Tasks";

const theme = {
  global: {
    colors: {
      white: "#FFFFFF",
      brand: "#FE8E4E",
      brandLight: "#FFD025",
      blackBorder: "#003c58",
    },
    font: {
      family: "Roboto",
      size: "20px",
      height: "24px",
    },
  },
};

function App() {
  return (
    <Grommet theme={theme}>
      <Router>
        <Page fill background="blackBorder">
          <PageContent background="white" style={{padding: "0"}}>
            <div style={{ "min-height": "100vh" }}>
              <Header background={"brand"}>
                <Link to="/">
                  <Heading margin="20px" color="white">
                    DVBAJSfPC
                  </Heading>
                </Link>
              </Header>
              <div style={{margin: "20px"}}>
                <Routes>
                  <Route path="/task" element={<TaskDescription />} />
                  <Route path="/tasks" element={<Tasks />} />
                  <Route path="/" element={<Home />} />
                </Routes>
              </div>
            </div>
          </PageContent>
        </Page>
      </Router>
    </Grommet>
  );
}

export default App;
