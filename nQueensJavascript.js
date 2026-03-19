const popSize = 100;
let generation = 0;
let running = true;

const canvas = document.getElementById("board");
const ctx = canvas.getContext("2d");

document.getElementById("capCheck").onchange = (e) => {
  document.getElementById("capInput").disabled = !e.target.checked;
};

document.getElementById("pause").onclick = () => running = false;
document.getElementById("resume").onclick = () => running = true;

document.getElementById("run").onclick = runAlgorithm;

function runAlgorithm() {
  const output = document.getElementById("output");
  output.value = "";

  let n = parseInt(document.getElementById("columns").value);
  let capEnabled = document.getElementById("capCheck").checked;
  let genCap = capEnabled ? parseInt(document.getElementById("capInput").value || 0) : 0;

  if (n < 4 || n > 20) {
    output.value += "Invalid input\n";
    return;
  }

  let configurations = [];
  let optimalFitness = 3 * n;

  generation = 1;

  for (let i = 0; i < popSize; i++) {
    let arr = Array.from({ length: n }, () => rand(1, n));
    arr.push(calcFitness(arr));
    configurations.push(arr);
  }

  let startTime = Date.now();

  function loop() {
    if (!running) {
      setTimeout(loop, 200);
      return;
    }

    let maxFitness = computeMaxFitness(configurations);

    document.getElementById("genLabel").innerText = "Generation: " + generation;
    document.getElementById("fitnessLabel").innerText = "Fitness: " + maxFitness;

    output.value += `Gen ${generation} | Fitness: ${maxFitness}\n`;

    let best = configurations.find(c => c[c.length - 1] === maxFitness);
    drawBoard(best, maxFitness === optimalFitness);

    if (maxFitness === optimalFitness) {
      let time = ((Date.now() - startTime) / 1000).toFixed(2);
      output.value += `\nSolved in ${time}s`;
      return;
    }

    if (genCap && generation >= genCap) return;

    configurations = createChildren(configurations);
    generation++;

    setTimeout(loop, 10);
  }

  loop();
}

function drawBoard(board, solved) {
  let n = board.length - 1;
  let size = canvas.width / n;

  for (let i = 0; i < n; i++) {
    for (let j = 0; j < n; j++) {
      ctx.fillStyle = (i + j) % 2 === 0 ? "white" : "gray";
      ctx.fillRect(j * size, i * size, size, size);

      if (board[i] === j + 1) {
        ctx.fillStyle = solved ? "green" : "red";
        ctx.beginPath();
        ctx.arc(j * size + size / 2, i * size + size / 2, size / 4, 0, Math.PI * 2);
        ctx.fill();
      }
    }
  }
}

function computeMaxFitness(configs) {
  return Math.max(...configs.map(c => c[c.length - 1]));
}

function createChildren(configs) {
  let totalFitness = configs.reduce((sum, c) => sum + c[c.length - 1], 0);

  let weights = configs.map(c => (c[c.length - 1] / totalFitness) * 100);
  for (let i = 1; i < weights.length; i++)
    weights[i] += weights[i - 1];

  let newPop = [];

  while (newPop.length < popSize) {
    let p1 = configs[getParent(weights)];
    let p2 = configs[getParent(weights)];

    while (JSON.stringify(p1) === JSON.stringify(p2))
      p2 = configs[getParent(weights)];

    let mid = Math.floor(p1.length / 2);

    let c1 = [...p1.slice(0, mid), ...p2.slice(mid)];
    let c2 = [...p2.slice(0, mid), ...p1.slice(mid)];

    if (Math.random() < 0.1) c1 = mutate(c1);
    if (Math.random() < 0.1) c2 = mutate(c2);

    newPop.push(c1, c2);
  }

  return newPop;
}

function mutate(arr) {
  let n = arr.length - 1;
  let i = rand(0, n - 1);
  arr[i] = rand(1, n);
  arr[n] = calcFitness(arr);
  return arr;
}

function getParent(weights) {
  let r = Math.random() * weights[weights.length - 1];
  return weights.findIndex(w => r <= w);
}

function calcFitness(conf) {
  let n = conf.length - 1;
  let fitness = 3 * n;

  for (let i = 0; i < n; i++) {
    for (let j = i + 1; j < n; j++) {
      if (conf[i] === conf[j]) fitness -= 2;
      if (Math.abs(i - j) === Math.abs(conf[i] - conf[j])) fitness -= 2;
    }
  }

  return fitness;
}

function rand(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}
