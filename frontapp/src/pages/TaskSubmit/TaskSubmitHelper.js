const TASK_OPTIONS = ["A-Josko posrat", "B-Jidko nasrat", "C-Silno obosratsa"];
const PL_OPTIONS = ["cpp", "python", "scala", "java"];

function toTitleCase(str) {
  return str.replace(/\w\S*/g, function (txt) {
    return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
  });
}

export {PL_OPTIONS, TASK_OPTIONS, toTitleCase}