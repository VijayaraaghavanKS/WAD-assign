const symbols = [
    "🍎","🍌","🍇","🍉",
    "🍓","🍒","🥝","🍍"
];

let cards = [];
let cardState = [];

let firstCard = -1;
let secondCard = -1;

let flipCount = 0;
let clickDisabled = false;

const gameBoard = document.getElementById("gameBoard");
const restartBtn = document.getElementById("restartBtn");

function startGame()
{
    cards = [...symbols, ...symbols];

    cards.sort(() => Math.random() - 0.5);

    cardState = new Array(16).fill("hidden");

    firstCard = -1;
    secondCard = -1;
    flipCount = 0;
    clickDisabled = false;

    createBoard();
}

function createBoard()
{
    gameBoard.innerHTML = "";

    for(let i = 0; i < 16; i++)
    {
        const card = document.createElement("div");

        card.classList.add("card");
        card.classList.add("hidden");

        card.dataset.index = i;
        card.textContent = cards[i];

        card.addEventListener("click", flipCard);

        gameBoard.appendChild(card);
    }
}

function flipCard()
{
    const index = this.dataset.index;

    if(clickDisabled)
        return;

    if(cardState[index] !== "hidden")
        return;

    cardState[index] = "flipped";

    this.classList.remove("hidden");
    this.classList.add("flipped");

    flipCount++;

    if(flipCount === 1)
    {
        firstCard = index;
    }
    else if(flipCount === 2)
    {
        secondCard = index;

        clickDisabled = true;

        setTimeout(checkMatch, 1000);
    }
}

function checkMatch()
{
    const allCards = document.querySelectorAll(".card");

    if(cards[firstCard] === cards[secondCard])
    {
        cardState[firstCard] = "matched";
        cardState[secondCard] = "matched";

        allCards[firstCard].classList.remove("flipped");
        allCards[secondCard].classList.remove("flipped");

        allCards[firstCard].classList.add("matched");
        allCards[secondCard].classList.add("matched");
    }
    else
    {
        cardState[firstCard] = "hidden";
        cardState[secondCard] = "hidden";

        allCards[firstCard].classList.remove("flipped");
        allCards[secondCard].classList.remove("flipped");

        allCards[firstCard].classList.add("hidden");
        allCards[secondCard].classList.add("hidden");
    }

    firstCard = -1;
    secondCard = -1;

    flipCount = 0;
    clickDisabled = false;

    checkWin();
}

function checkWin()
{
    let completed = true;

    for(let state of cardState)
    {
        if(state !== "matched")
        {
            completed = false;
            break;
        }
    }

    if(completed)
    {
        setTimeout(() => {
            alert("Congratulations! You matched all cards.");
        }, 300);
    }
}

restartBtn.addEventListener("click", startGame);

startGame();