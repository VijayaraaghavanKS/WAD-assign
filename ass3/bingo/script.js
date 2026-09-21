// ---- Game state ----
// card1/card2: 25 numbers (1-25 shuffled) laid out as a 5x5 grid, index 0-24 row by row.
// marked1/marked2: Set of numbers that have been marked on that player's card.
let card1 = [], card2 = [];
let marked1 = new Set();
let marked2 = new Set();
let gameStarted = false;
let currentPlayer = 1; // whose turn it is to enter a number

const board1El = document.getElementById('board1');
const board2El = document.getElementById('board2');
const turnText = document.getElementById('turnText');
const winText = document.getElementById('winText');

// Returns a random 1-25 permutation using Fisher-Yates shuffle.
function generateCard() {
  const numbers = Array.from({ length: 25 }, (_, i) => i + 1);
  for (let i = numbers.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [numbers[i], numbers[j]] = [numbers[j], numbers[i]];
  }
  return numbers;
}

// Draws a card's 25 cells into its board div, marking cells present in markedSet.
function renderBoard(boardEl, card, markedSet) {
  boardEl.innerHTML = '';
  card.forEach((num) => {
    const cell = document.createElement('div');
    cell.className = 'cell' + (markedSet.has(num) ? ' marked' : '');
    cell.textContent = num;
    boardEl.appendChild(cell);
  });
}

// A player wins if any full row, column, or diagonal (of the 5x5 grid) is entirely marked.
function hasWon(card, markedSet) {
  const isMarked = (row, col) => markedSet.has(card[row * 5 + col]);

  for (let row = 0; row < 5; row++) {
    if ([0, 1, 2, 3, 4].every((col) => isMarked(row, col))) return true;
  }
  for (let col = 0; col < 5; col++) {
    if ([0, 1, 2, 3, 4].every((row) => isMarked(row, col))) return true;
  }
  if ([0, 1, 2, 3, 4].every((i) => isMarked(i, i))) return true;
  if ([0, 1, 2, 3, 4].every((i) => isMarked(i, 4 - i))) return true;

  return false;
}

document.getElementById('startBtn').addEventListener('click', () => {
  card1 = generateCard();
  card2 = generateCard();
  marked1 = new Set();
  marked2 = new Set();
  gameStarted = true;
  currentPlayer = 1;

  renderBoard(board1El, card1, marked1);
  renderBoard(board2El, card2, marked2);
  board1El.classList.remove('winner');
  board2El.classList.remove('winner');
  winText.textContent = '';
  turnText.textContent = "Player 1's turn";
});

document.getElementById('resetBtn').addEventListener('click', () => {
  card1 = [];
  card2 = [];
  marked1 = new Set();
  marked2 = new Set();
  gameStarted = false;
  board1El.innerHTML = '';
  board2El.innerHTML = '';
  board1El.classList.remove('winner');
  board2El.classList.remove('winner');
  turnText.textContent = 'Click Start to begin';
  winText.textContent = '';
  document.getElementById('numberInput').value = '';
});

document.getElementById('markBtn').addEventListener('click', () => {
  if (!gameStarted) {
    alert('Click Start first.');
    return;
  }

  const input = document.getElementById('numberInput');
  const num = parseInt(input.value, 10);
  if (!num || num < 1 || num > 25) {
    alert('Enter a number between 1 and 25.');
    return;
  }

  // The entered number is marked on BOTH cards, whichever card contains it.
  marked1.add(num);
  marked2.add(num);
  renderBoard(board1El, card1, marked1);
  renderBoard(board2El, card2, marked2);
  input.value = '';

  const player1Won = hasWon(card1, marked1);
  const player2Won = hasWon(card2, marked2);

  if (player1Won || player2Won) {
    gameStarted = false; // stop further marking once someone has won
    if (player1Won) board1El.classList.add('winner');
    if (player2Won) board2El.classList.add('winner');

    if (player1Won && player2Won) {
      winText.textContent = "It's a tie! Both players got Bingo.";
    } else {
      winText.textContent = `Player ${player1Won ? 1 : 2} wins! Bingo!`;
    }
    turnText.textContent = 'Game over';
    return;
  }

  // No winner yet, switch turn to the other player.
  currentPlayer = currentPlayer === 1 ? 2 : 1;
  turnText.textContent = `Player ${currentPlayer}'s turn`;
});
