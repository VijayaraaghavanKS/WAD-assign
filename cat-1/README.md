# Memory Card Matching Game

## Course Information

**Course Name:** Web Application Development  
**Course Code:** ICS1503  
**Assignment:** CAT Assignment 1  
**Title:** Memory Card Matching Game using HTML, CSS and JavaScript

**Name:** Vijayaraaghavan K S 
**Registration Number:** 3122247001066 

---

# 1. Website Overview

The Memory Card Matching Game is a browser-based interactive game developed using HTML, CSS, and JavaScript.

The objective of the game is to find and match all pairs of cards by flipping two cards at a time. The game consists of 16 cards arranged in a 4 × 4 grid, containing 8 matching pairs. All cards are initially displayed face down. The player flips cards to reveal their contents and attempts to locate matching pairs.

The application uses JavaScript to manage card states, validate matches, control user interactions, and determine game completion.

---

# 2. Features Implemented

### 2.1 4 × 4 Card Grid
- The game displays 16 cards arranged in a 4 × 4 layout.
- Each card contains one symbol.
- Every symbol appears exactly twice.

### 2.2 Random Card Shuffling
- The card positions are shuffled every time the game starts.
- This ensures a different card arrangement for each game.

### 2.3 Face-Down Cards
- All cards are initially hidden.
- The user cannot see card values until they are flipped.

### 2.4 Two-Card Flip Restriction
- The player can flip only two cards at a time.
- Additional clicks are disabled until the current pair is processed.

### 2.5 Matching Logic
- If both selected cards contain the same symbol:
  - They remain visible.
  - Their state changes to **matched**.

### 2.6 Mismatch Logic
- If the selected cards do not match:
  - They remain visible for 1 second.
  - They automatically flip back.

### 2.7 Game Completion Detection
- The system continuously checks whether all cards are matched.
- A congratulatory message is displayed when all pairs are found.

### 2.8 Restart Functionality
- A Restart Game button allows the user to begin a new game.
- Cards are reshuffled and all states are reset.

### 2.9 Responsive Design
- The layout adapts to smaller screen sizes.
- The game remains usable on both desktop and mobile devices.

---

# 3. Folder Structure Explanation

```
MemoryCardGame/
│
├── index.html
├── style.css
├── script.js
└── README.md
```

### index.html
Contains the structure of the webpage including:
- Game title
- Restart button
- Game board container

### style.css
Contains all styling related to:
- Grid layout
- Card appearance
- Responsive design
- Visual states of cards

### script.js
Contains the complete game logic including:
- Card shuffling
- State management
- Matching verification
- Win detection

### README.md
Contains project documentation and implementation details.

---

# 4. Logic Used in the Implementation

The game logic is based on **two arrays and a state-management approach**.

---

## 4.1 Card Data Array

A card array stores the shuffled symbols displayed on the board.

Example:

```javascript
cards = [
"🍎","🍌","🍇","🍓",
"🍎","🍌","🍇","🍓",
...
]
```

After duplication, the array is shuffled randomly.

Purpose:
- Stores the actual values of all cards.
- Used during match comparison.

---

## 4.2 Card State Array

A second array maintains the state of every card.

Example:

```javascript
cardState = [
"hidden",
"hidden",
"matched",
"flipped",
...
]
```

Three states are used:

### Hidden
- Card is face down.
- Player cannot see its symbol.

### Flipped
- Card is temporarily revealed.
- Waiting for comparison.

### Matched
- Card has found its pair.
- Remains permanently visible.

Purpose:
- Tracks current status of every card.
- Prevents illegal moves.
- Simplifies match verification.

---

## 4.3 Global Flip Counter

A global variable is used to count currently flipped cards.

```javascript
let flipCount = 0;
```

Behaviour:

### First Flip
- Stores the first selected card.
- Increments counter to 1.

### Second Flip
- Stores the second selected card.
- Increments counter to 2.
- Automatically triggers match checking.

Purpose:
- Ensures only two cards are processed at a time.

---

## 4.4 Click Disabling Mechanism

A Boolean variable is used:

```javascript
let clickDisabled = false;
```

When two cards are flipped:

```javascript
clickDisabled = true;
```

This temporarily disables additional clicks until:

- Cards match, or
- Cards are flipped back.

Purpose:
- Prevents users from flipping more than two cards simultaneously.
- Satisfies assignment requirement 7.

---

# 5. Functions Used

---

## 5.1 startGame()

### Purpose

Initializes the game.

### Operations

- Creates card pairs.
- Shuffles cards.
- Resets states.
- Resets counters.
- Generates the board.

### Function Call

```javascript
startGame();
```

---

## 5.2 createBoard()

### Purpose

Creates the 16 card elements dynamically.

### Operations

- Clears old board.
- Creates card divs.
- Assigns index values.
- Attaches click event listeners.
- Displays cards on screen.

### Function Call

```javascript
createBoard();
```

---

## 5.3 flipCard()

### Purpose

Handles card selection.

### Operations

- Checks if clicking is allowed.
- Checks card state.
- Reveals card.
- Updates state array.
- Updates flip counter.
- Triggers matching process after second card.

### Event Listener

```javascript
card.addEventListener("click", flipCard);
```

---

## 5.4 checkMatch()

### Purpose

Compares two selected cards.

### Operations

If cards match:

```javascript
cardState[firstCard] = "matched";
cardState[secondCard] = "matched";
```

If cards do not match:

```javascript
cardState[firstCard] = "hidden";
cardState[secondCard] = "hidden";
```

Also:
- Resets counters.
- Re-enables clicking.
- Calls win checking function.

### Function Call

```javascript
setTimeout(checkMatch, 1000);
```

---

## 5.5 checkWin()

### Purpose

Determines whether all cards have been matched.

### Logic

Traverses the entire state array.

```javascript
for(let state of cardState)
```

Checks:

```javascript
if(state !== "matched")
```

If every card is matched:

```javascript
alert("Congratulations!");
```

Purpose:
- Detects game completion.

---

# 6. Algorithms Used

### Card Shuffling

```javascript
cards.sort(() => Math.random() - 0.5);
```

Used to randomize card positions before every game.

---

### Match Detection

```javascript
if(cards[firstCard] === cards[secondCard])
```

Compares symbols stored in the card array.

---

### State Management

The game uses a finite-state approach:

```
Hidden → Flipped → Matched
```

or

```
Hidden → Flipped → Hidden
```

depending on comparison results.

---

# 7. Assignment Requirements Mapping

| Requirement | Implementation |
|------------|---------------|
| 16 cards in 4×4 grid | Implemented using CSS Grid |
| Cards initially face down | Hidden state |
| Random shuffle | Math.random() shuffle |
| Flip only two cards | flipCount variable |
| Matching cards stay visible | Matched state |
| Non-matching cards flip back | setTimeout() after 1 second |
| Prevent more than two flips | clickDisabled flag |
| Separate HTML, CSS, JS files | Implemented |
| Responsive design | Media query added |

---

# 8. Conclusion

The Memory Card Matching Game was developed using HTML, CSS, and JavaScript by implementing a two-array state-management approach. One array stores the shuffled card values while another array stores the current state of each card. A global flip counter and click-disabling mechanism ensure that only two cards can be processed at a time. The application satisfies all assignment requirements while maintaining clean code structure, responsiveness, and interactive gameplay.