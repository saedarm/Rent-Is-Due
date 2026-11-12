# Rent's Due

*An Uncle Rob game. Working title.*

A stupid landlord board game for 2–6 players that runs in a computer's browser, with a small server for Fly.io. Built with libGDX and compiled to JavaScript with TeaVM. Play against bots offline, or online with friends plus bots.

It's a lap-around-the-board property game (buy rentals, charge each other rent, trade, go to jail) with a fixed length like The Game of Life: it ends after 12 months and whoever is worth the most wins. Every name, card and joke is new.

## How a game works

- **Goal:** be worth the most after 12 months (8 for a quick game, 16 for a long one). Worth = cash + what your rentals cost + upgrades, minus $100 per IOU.
- Everyone starts with $700 (the lobby can change it). Later seats get $25 more each.
- **Your turn:** play a keeper card or offer a trade if you want, then roll two dice and move clockwise.
- Doubles roll again. Doubles twice in a row means you're too wound up, and you go to Wing Jail.
- **A month** is one trip around the table. Each new month starts with a Town News event that hits everybody.

### Rent is collected by hand

This is the Rob rule. Every month, each rental you own gets a month of tenant rent stuffed into its envelope (up to 3 months). The money is not yours until you drive past the house. You pick up an envelope when you pass or land on your own rental. Until then it sits on the porch, and other players have cards that take it.

### Landing on things

| Square | What happens |
| --- | --- |
| A rental nobody owns | Buy it or pass. If you pass, the bank auctions it to everybody else (see Auctions). |
| Somebody else's rental | You crash there and owe rent: 25% of the price, more if it's fixed up, 50% more if they own two in that neighborhood, double if they own all three. If the rent is $30 or more you choose: **pay it**, or **sneak out the back** and roll. 5–6 you get away free, 3–4 you pay, 1–2 you're caught and pay double. |
| Your own rental | Pick up the envelope. You can fix it up: fresh paint, then granite countertops. Each costs half the price and raises rent and envelopes. |
| First of the Month | +$150 every time you pass. |
| Rob Happens | Click the deck to draw. Your card goes up for the whole table, then you play it. Some happen right away, some are keepers. |
| Tenant Trouble | Click the deck to draw a card about your rentals. If you have none: "No tenants, no problems," +$20. |
| Water Tower Gig, Pool Gig | Choose: **the sure thing** ($80), or **the big job** and roll. 1–2 the customer ghosts you, 3–6 pays $35 times the roll (up to $210). |
| Frickers | Choose: **takeout** ($30 into the Coffee Can), or **the wing contest** ($60 to enter, a 5 or 6 wins $240). Wing Jail is in the back. |
| Wing Night (corner) | It got out of hand. Go directly to Wing Jail. |
| The Coffee Can | The can of cash in Rob's truck. Fees and fines go in. Land here and take it all. |
| Lowe's | Choose: **just the washer** ($20 into the can), or **look around** and roll. 1–3 you come out with a pressure washer (-$90). 4–6 you find the clearance aisle: free upgrade on one of your rentals, or $60 if there's nothing to upgrade. |
| Call Jules | Choose: **text her** (she sends back "k", +$40), or **call her** and roll. 4 or more and she picks up (+$120); otherwise voicemail. If you're playing Jules, you call your dad and he always picks up. |
| The Ex's House | Choose: **fix her garbage disposal** ($30 in parts), or **drive by slowly** and roll. 1–2 she waves you in and you do the gutters too (-$70). 3–6 clean getaway. |
| SQUIRREL! | Roll a die. Even: forward that many. Odd: back that many. Then deal with wherever you ended up. |

### Wing Jail

Three ways in: the Wing Night corner, the Wing Night card, or doubles twice in a row. Your piece flies to Frickers, wings rain down the board, and a picture of your face behind celery bars drops in.

On your turn in jail, pick one:

- **Eat:** roll the dice. Doubles gets you out and you move that many. No doubles: "Another basket..." and you stay.
- **Pay the $50 tab:** you're out, and you roll normally.

After three failed tries they kick you out, you pay the tab anyway, and you move. While you're inside you can't pick up envelopes, but visitors still pay you rent.

### Neighborhoods

The 24 rentals come in eight neighborhoods of three. Own two in one and visitors pay 50% more there. Own all three and they pay double, plus you get its perk and a banner with your face on it.

The tracker in the middle of the board shows all eight neighborhoods with three dots each, coloured by owner. Hover a rental and the rest of its neighborhood lights up.

| Neighborhood | Rentals | Perk for owning all three |
| --- | --- | --- |
| Huber Heights | Brick Ranch #1 ($60), Brick Ranch #2 (Identical) ($60), Brick Ranch #3 (Also Identical) ($80) | Every Ranch Is The Same Ranch: your envelopes here fill two months at a time. |
| Fairborn | The Duplex With The Smell ($100), Garage Apartment (No Garage) ($100), The One By The Base (Jets Included) ($120) | Base Housing: +$50 from the bank every month. |
| Vandalia | The House Rob Lived In For 3 Weeks ($140), Above The Vape Shop ($140), Flight Path Bungalow (Loud) ($160) | Air Show Weekend: visitors pay triple instead of double. |
| Kettering | The One With The Green Pool ($180), Four-Plex On Needmore ($180), Condo With The HOA Lady ($200) | HOA President: Tenant Trouble cards never cost you money. |
| Tipp City | Lake House (It's A Pond) ($220), Half A Double, Left Half ($220), Half A Double, Right Half ($240) | Small Town: +$20 whenever somebody else draws a Rob Happens card. |
| Centerville | The Flip Rob Never Finished ($260), Cul-De-Sac Special ($260), The Split-Level (Stairs Everywhere) ($280) | Good Schools: fixing up any of your rentals costs half. |
| Beavercreek | Townhouse Behind The Mall ($300), The Nice One By The Golf Course ($300), The One With The Bonus Room ($320) | Behind The Mall: +$100 extra every time you pass First of the Month. |
| Oakwood | The Oakwood Colonial ($350), Carriage House (No Carriage) ($350), The Good House (Don't Touch Anything) ($400) | Old Money: other players' keeper cards can't touch you. |

Landing on all three yourself is rare. Trading is how you finish a neighborhood.

### Trading

- Before you roll, hit **TRADE** (or press T). One offer per turn.
- Click your own rentals to offer them. Click another player's rentals to ask for them. Up to four each way.
- **ADD $50** sweetens it. **ASK FOR $50** asks them for cash.
- **SEND OFFER** puts it in the middle of the board for everyone to see. The other player hits DEAL or NO DEAL.
- Online they get 20 seconds. If the clock runs out, it's no deal.
- Upgrades and whatever's in the envelope go with the rental.
- Bots make offers too. When a bot has two of a neighborhood and you have the third, it offers 1.8 times what yours is worth. Bots take a trade if it pays them at least 20% over value, or 75% over if it finishes your neighborhood.

### Auctions

When somebody passes on a rental, the bank auctions it. The player who passed sits out.

- Bidding opens at half the price and goes up in steps of 10% of the price.
- It goes around the table. On your turn to bid, you either bid the next amount or drop out for good.
- The last one left pays their bid and gets the rental. If nobody bids, it stays unowned.
- Online you get 12 seconds per bid. If the clock runs out, you're out.
- Bots bid up to about 85% of the price, and more if they already own something in that neighborhood.

### While you wait

Things you can do when it isn't your turn:

- **Bet on the roll.** Before another player rolls, bet $25 on HIGH (8 or more) or LOW (6 or less). A 7 loses either way. Win and the bank pays you $25. Lose and your $25 goes into the Coffee Can. During a Power Outage (one die), 4–6 is high and 1–3 is low. Bots bet about a third of the time.
- **Call a contractor.** Click any rental you own, any time, and fix it up without landing on it. It costs 50% more than doing it yourself.
- **Call the tenant.** Same panel. Can't get over there? They Venmo you half of what's in the envelope, right now, and keep the rest.
- **Sell to the bank.** Same panel. You get half the rental's value.
- **Taunt.** The five buttons above your cards, or keys 1–5.
- **Play Wing Run.** See below.

### Wing Run (the side game)

Hit the orange WING RUN button next to the taunts and the game log turns into a little arcade game. It only runs while you're waiting; when it's your turn it pauses and says YOUR TURN.

- You are shirtless Rob. Arrow keys or WASD run him around a grid of Frickers tables.
- Eat every wing to clear the basket. They bring another, and the salads get faster.
- Three salads chase you. Touch one and you "ate a salad": you lose 5 wings and start over in the middle.
- Two hot sauce bottles sit in the corners. Grab one and for 5 seconds Rob is faster and can flatten salads for +5 each.
- It doesn't pay any money. Your wing count goes to the table, and whoever ate the most gets the **Wing King** award at the end of the year.
- Everything runs on your own screen. Nobody else sees your game.

### Table rules (in the lobby)

The lobby leader clicks a rule to change it. Everyone sees the current setting.

| Rule | Choices |
| --- | --- |
| Months | 12, 16, 8 |
| Speed | Normal, Fast (1.7x), Turbo (3x). Dice, hops, cards and banners all play faster. |
| Bot turns | Extra fast (another 1.8x on top), or the same speed as people |
| Starting cash | $700, $1,000, $500 |
| Trading | On or off |
| Auctions | On or off |
| Side bets | On or off |
| Turn clock | 45 seconds, 90 seconds, or off. Online only. Trade offers always get 20 seconds unless the clock is off. |

An 8-month game against three bots on Turbo took under two minutes in testing, with a script clicking instantly for the human.

### Going broke

Nobody gets knocked out. If you can't pay, the bank takes your cheapest rental for half its value, and keeps going until you can. If you're out of rentals too, you pay what you have and take an IOU to Rob, worth -$100 at the end.

## Characters

Rob, Jules, Sam and Jack use their photos. Todd and Brenda are drawn. If your name has "rob", "jules", "sam" or "jack" in it, you get that character when you sit down, if it's free.

| Character | Perk |
| --- | --- |
| **Uncle Rob** | Collects In Person: his envelopes hold 50% more. Nobody stiffs a guy standing on the porch. |
| **Jules** | Good With Phones: every month, Venmo pulls one month of rent out of her fattest envelope. No driving. |
| **Sam** | Reconciles The Books: finds +$75 extra every time he passes First of the Month. |
| **Jack** | Introvert: gone before anyone notices. Pays a third of the rent at other people's rentals. |
| **Hedge Fund Todd** | Cash Offer: rentals cost him 15% less. |
| **Flipper Brenda** | Staged It: her rentals count 15% extra in the final tally. |

## Cards

Every card has a picture, many with Rob's face on a stick figure. The twelve newest Rob Happens cards, six newest Tenant Trouble cards and four newest Town News events borrow pictures from older cards.

**Drawing a card:** when you land on a card square, you click the deck yourself. The card flips up in the middle of the board for everyone and stays there until you hit OK. In a browser it is also read out loud on every player's computer (turn that off in Settings). Bots hold their card up for a couple of seconds.

### Rob Happens

| Card | What it does |
| --- | --- |
| Wing Night | Go directly to Wing Jail. |
| Where'd I Park The Truck | Skip your next turn. |
| Water Tower Call | +$120. You smell like a pool for a week. |
| Bought Another One | You buy the cheapest rental left, at half price, without looking at it. |
| Rob Moves Again | Swap places with a random player. |
| Bought A Round | Pay every other player $20. |
| Glovebox Check | Found a rent check from March. +$150. |
| Tried The Venmo | Sent $80 to the wrong person. A random player says thanks. |
| Five Projects, Zero Finished | $60 into the Coffee Can. |
| Reds Lost Again | $30 of ballpark beer into the Coffee Can. |
| Jules Texted Back | It said "k". Best day of the month. +$60. |
| The Ex Needs Her Gutters Done | Go to The Ex's House. |
| Chlorine Spill | Truck's never been cleaner. -$70. |
| Gas Station Energy Drink, 9 PM | Take another turn right now. |
| Ooh, What's That | Roll a die and move back that many. |
| Garage Sale | Sold three of your seven ladders. +$70. |
| The Good Cooler | The ex still has it. You let her keep it. -$40. |
| Tried To Relax | Sat down for eleven seconds. Mowed a tenant's lawn instead. +$40. |
| Scratch-Offs | Bought twenty at the Speedway. Won on one. +$50. |
| Friday Fish Fry | You bought dinner for the whole table. -$45. |
| Free Couch On The Curb | You put it in a rental and call the place "furnished." +$60. |
| Read The Meter Wrong | At your own house. -$55. |
| Hit A Deer On 75 | The bumper is in the truck bed. -$90. |
| The Menards Rebate Came | Eleven percent, in store credit. +$35. |
| Started A Podcast | "Landlord Talk." One listener. It's Jules. +$20. |
| Bought A Boat | It has never been in water. $120 into the Coffee Can. |
| Thirty Keys, No Labels | Skip your next turn trying them all. |
| Knows A Shortcut | It is not shorter. Move ahead 5 spaces. |
| Double-Booked | Two showings, same time. Go back 3 spaces. |
| Tailgate | You brought the brats. Every other player pays you $15. |

**Keepers** (two of each in the deck; hold up to 3; play before you roll):

| Card | What it does |
| --- | --- |
| Rent's Due! | Every other player pays you $40. |
| Anonymous Tip | Whoever's winning pays $100 into the Coffee Can. |
| Poach The Rent | Grab the fattest uncollected envelope on somebody else's rental. |
| Sheriff's Sale | Buy the leader's cheapest rental off them for what it's worth. They don't get a say. |
| Long Story | The leader skips their next turn. |
| I Know A Guy | Free upgrade on your best rental. |

### Tenant Trouble

Water Heater Blew (-$60), Paid In Quarters (collect every envelope now), Paid In Wings (your fattest envelope is gone), Raccoons In The Attic (-$50), Tenant "Improved" The Place (free upgrade), Skipped Town (lose an envelope, -$30), Surprise Inspection ($20 per rental into the can), Good Tenant (an extra month in every envelope), Cousin "Just Visiting" (-$40), Property Tax Bill (5% of your rentals' value into the can), Nobody's Home (skip a turn), Tenant Fixed The Furnace (+$80), Green Pool (+$100), Late Fees (+$15 per rental), Handshake Lease (roll: -$50 or +$90), "Emotional Support" Horse (-$45), Grill On The Balcony (-$35), Tenant Referred A Friend (+$70), Painted It Black (-$65), Six Months Up Front (+$120), Planted Tomatoes (+$25).

### Town News (one a month, hits everybody)

| Event | What it does |
| --- | --- |
| Polar Vortex | Everyone pays $15 per rental. |
| All-You-Can-Eat Wings | Everyone drops $25 into the Coffee Can. |
| Housing Boom | Visitor rent is up 50% this month. |
| Boil Advisory | Everyone gets $40, and gigs pay double this month. |
| Tax Season | Everyone pays 10% of their cash into the Coffee Can. |
| Reds Win! | Everyone gets $50. |
| Rent Strike | No new rent goes into the envelopes this month. |
| County Fair | $100 goes into the Coffee Can. |
| Black Friday At Lowe's | Upgrades are half price this month. |
| Power Outage | Everyone rolls one die this month. (In Wing Jail, a 6 gets you out.) |
| Market Dip | Unowned rentals are 25% off this month. |
| Leaf Pickup Week | Everybody moves back 2 spaces. |
| Rob Learns Venmo (For One Day) | Everyone collects their fattest envelope without driving anywhere. |
| Orange Barrel Season | Every roll moves half as far this month. |
| Wing Shortage | Everyone in Wing Jail walks free, and nobody goes in this month. |
| Lottery Fever | The Coffee Can doubles (up to $300 more). |

### End of the year

The game ranks everyone by worth and hands out awards from what actually happened: Landlord of the Year (most rentals), Frickers Regular (most trips to Wing Jail), Collects By Hand (most rent picked up in person), Wing King (most wings eaten in Wing Run), Let's Make A Deal (most trades), I'm Good For It (most IOUs).

## Menus

- **Main menu:** the title, a picture of Rob holding an envelope and a wing in front of his truck and five identical brick ranches, and four buttons: Play, How To Play, Settings, Quit. In a browser, Quit goes to a goodbye screen, because a tab can't close itself; on desktop it closes the game.
- **Play:** type your name, then Play vs Bots or Play Online.
- **Settings:** sound on/off and volume, music on/off and volume, screen shake on/off, and (in a browser) read cards out loud on/off. All remembered, along with your name.
- **How To Play:** nine pages with pictures. It opens by itself the first time you visit. Also in the lobby, in Settings and in the in-game menu.
- **Lobby:** seats on the left, table rules on the right.
- **In a game:** MENU and QUIT buttons sit at the top of the board (Esc also works). Both open the menu: Back To The Game, How To Play, Settings, and Quit To Main Menu. The lobby and the results screen have their own Quit buttons.

## What you see and hear

- Pieces are faces in a ring of the player's colour. They hop square by square, and arc across the board when a card sends them somewhere.
- Two dice tumble and land. Single-die rolls (gigs, Frickers, Call Jules) get one die.
- Cards slide out of their deck and flip over in the middle of the board, picture on top.
- Pictures pop up when you land on Lowe's, SQUIRREL!, Call Jules (she picks up, or voicemail), The Ex's House and the Coffee Can.
- The Wing Jail show: flight to Frickers, raining wings, screen shake, your face behind celery bars. Jailed pieces sit behind celery bars on the Frickers square.
- Coins fly between players, the bank and the Coffee Can, with +$ and -$ floating up.
- Envelopes show on each rental as a green tag with the amount waiting.
- SOLD!, FRESH PAINT, GRANITE!, YOINK! and BANK'S NOW stamps.
- Speech bubbles, plus five taunts you can fire any time (the buttons above your cards, or keys 1–5).
- Online, a countdown bar shows how long the current decision has left.
- Hover a square, a player or one of your keepers to read what it does, including the neighborhood perk.
- Banners across the board for the start of the game, each new month, and LAST MONTH!
- A neighborhood banner when somebody completes one: their face, a crown, the perk, confetti in the neighborhood's colour.
- A crown on whoever is worth the most, on the board and in the player list. The winner wears it on the results screen.
- "Sam's turn" with the face under the month counter.
- Player rows flash green when money comes in and red when it goes out.
- A shower of coins when somebody wins the Coffee Can, and an IOU picture when somebody goes broke.
- Wings drift down the title screen.
- 15 sound effects: dice, hops, coins, card flip, cha-ching, upgrade, sad trombone, the jail buzzer, your-turn chime, trade bell, neighborhood fanfare, win fanfare.
- Music: a 33-second oom-pah loop, quiet, with its own on/off switch. It starts on your first click because browsers don't allow sound before that.

### Keys

| Key | What it does |
| --- | --- |
| Space | Roll, draw, play your card, buy, fix up, eat, bid, or take a deal |
| T | Start a trade |
| 1–5 | Taunts |
| Esc | Menu (or close whatever's open) |
| Click your own rental | Contractor, call the tenant, or sell |
| Arrows or WASD | Run Rob around in Wing Run |

## Running it

You need Java 17 or newer.

**In the browser, with the server (the real thing):**

```
gradlew server:installDist web:gdx_teavm_web_js_build
gradlew server:run
```

Then open `http://localhost:8080`. "Play online" joins the table on that server. "Play vs bots" runs the whole game inside the page with no server.

- `http://localhost:8080/?name=Jack` fills in the name.
- `http://localhost:8080/?auto` plays a bot game on its own so you can watch.

**Desktop window (fastest to iterate):**

```
gradlew lwjgl3:run
gradlew lwjgl3:run -Dmode=auto                         bots play, you watch
gradlew lwjgl3:run -Dserver=ws://localhost:8080/ws     where "Play online" goes
```

**Balance check (headless bot games):**

```
gradlew core:sim -Pgames="4000 4 12"      games, players, months
```

## Putting it on Fly.io

One small machine serves the page and runs the table. Everyone opens `https://your-app.fly.dev` in a browser. Nothing to install and no ports to open.

1. Install the Fly tool and sign in: `fly auth login`
2. Put an app name nobody has taken on the `app = ` line in `fly.toml` (it ships as `rents-due-sam`), then `fly apps create rents-due-sam`
3. `fly deploy --ha=false` (exactly one machine: two would split players across two tables)
4. Open `https://rents-due-sam.fly.dev`

`https://<app>.fly.dev/status` says who's in the lobby. The machine sleeps when nobody is connected and wakes on the next visit. The browser sends a ping every 20 seconds so Fly keeps the connection open. If the machine ever goes to sleep in the middle of a game, change `auto_stop_machines` to `"off"` in `fly.toml`.

I haven't run the Docker build or a real Fly deploy from here. The server and the browser build were tested locally in headless Chrome: full games by mouse against bots, a trade built and accepted, Wing Jail, the menus, and a two-browser online game. I couldn't listen to the sounds, the music or the read-aloud voice; they load and run without errors. The automatic reconnect is not tested. Auctions, choices and card draws were tested against bots only, not between two browsers.

**How the server works:** it runs the real game and the bots. Browsers send clicks (`act roll`, `act buy`, `act trade ...`) and get the whole table back as JSON after every change, plus a list of "beats" to animate (dice, hops, cards, coins). The server waits for those animations before the next bot moves.

- The first person in is the lobby leader: bots, number of months, START, and back to the lobby afterwards.
- One game at a time. Show up mid-game and you watch, then get a seat when the game goes back to the lobby.
- If you drop, a bot covers your seat and your row says so. The page tries to reconnect on its own up to three times and takes your seat back; after that, rejoin with the same name.
- Online, every decision has a clock (45 seconds unless the leader changes it), and a trade offer has 20 (`TRADE_SECONDS`). When a turn clock runs out, autopilot plays for you.
- Side bets, contractor calls and sales arrive as `side ...` messages from anybody at any time; they don't restart the turn clock.
- When everyone leaves, the server resets.

## Project layout

```
core/     rules (plain Java, no libGDX) and the libGDX client
lwjgl3/   desktop launcher
web/      browser launcher (TeaVM)
server/   Fly.io server (Javalin: static files + WebSocket). Compiles core's rules package directly.
assets/   fonts (Poppins Bold, SIL OFL), faces.png, pic/ (card and pop-up pictures), sfx/ (sounds)
tools/    make_art.py, make_sfx.py, make_music.py, make_font.py, photos/ (the source photos)
```

| File | What it does |
| --- | --- |
| `core/.../rules/Game.java` | The board, neighborhoods, every card, the characters and all the rules, including Wing Jail and trades. Stops with a prompt when it needs a decision; `act()` answers it. Records beats for the screen. Draws nothing. |
| `rules/Bot.java` | Computer players: what to buy, when to pay the tab, which trades to offer and take. Also the autopilot for timed-out humans. |
| `rules/Session.java` | Lobby, seats, lobby leader, bot pacing, clocks, taunts, sending state. Runs on the server and inside the client for offline games. |
| `rules/Sim.java` | Headless balance check. |
| `core/.../Main.java` | The whole client: title, lobby, board, animation, trade builder, menu, how-to-play, results. |
| `Ui.java` | Rectangles, circles, pictures, text and buttons on one SpriteBatch, on a 1280x720 virtual screen. |
| `WingRun.java` | The Wing Run side game. Self-contained: grid, Rob, salads, drawing. |
| `Link.java` | The wire to the table. `Link.Local` runs a `Session` inside the client for offline play. |
| `web/.../WebLauncher.java` | Browser entry point and the browser WebSocket. |
| `lwjgl3/.../DesktopLauncher.java` | Desktop entry point and the desktop WebSocket. |
| `server/.../Server.java` | The server. |

## Changing things

- **Squares and rentals:** the `BOARD` list in `Game.java`. Names, prices and neighborhoods are data. The short tag is what's printed on the board; keep each word to about 8 letters on the top and bottom rows.
- **Neighborhood perks:** text in `HOOD_PERK`; search `Game.java` for `ownsHood(` to find each effect.
- **Cards:** `ROBC`, `TENC` and `TOWNC` in `Game.java`. Each has a key; its effect is the matching `case` in `robCard()`, `tenantCard()`, `trick()` or `newRound()`.
- **Character perks:** text in `CH_PERK`; search `Game.java` for `ROB`, `JULES_CH`, `SAM`, `JACK`, `TODD`, `BRENDA`.
- **Wing Jail:** `toJail()` and the `JAIL` branch of `act()`. The tab is `TAB`.
- **Taunts:** `TAUNTS` in `Game.java`.
- **Wing Run:** `WingRun.java`. Grid size, speeds and the salad count are at the top; the Rob sprite is the `BODY` crop in `tools/make_art.py` (photo in `tools/photos/rob_body.png`).
- **Off-turn actions:** `side()` and `settleBets()` in `Game.java`. The bet size is `BET`.
- **Choices on squares:** each one is an `ask(...)` line in `land()` and a matching `case` in `choose()` in `Game.java`.
- **Auctions:** `startAuction()` and `nextBidder()` in `Game.java`.
- **A new money card:** one line with `m(key, title, text, amount, BANK or CAN, picture)` in `ROBC` or `TENC`. A positive amount pays the player; a negative one is paid to the bank or the Coffee Can. No other code needed.
- **Table rules:** the `opt` case in `Session.java`. The choices are the `ROUNDS`, `CASH`, `CLOCK` and `SPEED` lists at the top.
- **Main menu picture:** `make_title()` at the bottom of `tools/make_art.py`.
- **Money:** starting cash is in the `Game` constructor. Rent is `rent()`, envelopes are `envUnit()`, upgrades are `upCost()`.
- **How bots trade:** `offer()` and `wants()` in `Bot.java`.
- **Animation timing:** `Game.dur()`. The server and the screen both read it.
- **Photos:** replace a file in `tools/photos/` and adjust its crop box in `PHOTOS` at the top of `tools/make_art.py`. Then run `python3 tools/make_art.py` (needs Pillow). That rebuilds the playing pieces and every picture that uses a face.
- **Pictures:** each one is a few lines in `make_pics()` in `tools/make_art.py`, saved as `assets/pic/<card key>.png`. Or drop your own 256x160 PNG in with that name. A card with no picture file just shows text.
- **Sounds:** edit `tools/make_sfx.py` and run it (needs numpy), or drop your own `.wav` with the same name into `assets/sfx/`.
- **Music:** edit the chords and tune at the top of `tools/make_music.py` and run it (needs numpy and ffmpeg), or drop your own `loop.mp3` into `assets/music/`.
- **How-to-play pages:** `TUTORIAL` at the top of `Main.java`.
- **Fonts:** `python3 tools/make_font.py path/to/Font.ttf` (needs Pillow).

### Last balance run (4,000 bot games, 4 players, 12 months)

- **Money:** average final worth $1,470, first-to-last gap about $800.
- **Rentals:** 69% owned by the end. A whole neighborhood gets completed in about half of games.
- **Trades:** about one accepted trade every two games (bots only offer cash for the third rental of a neighborhood).
- **Wing Jail:** 2.5 trips a game.
- **Characters:** Jules wins 27%, Todd and Brenda 25%, Rob 24%, Sam 23%, Jack 22%. A fair share is 25%.
- **Going first:** wins 26%.
- **2 players:** only 42% of rentals get bought, and Jules wins 56% of her games. Two-player games want 16 months.
- **6 players:** 84% of rentals owned.
- **IOUs:** bots never take one, because they keep a cash cushion. People won't.

The bots haven't been tuned against real players. Tune from actual play.

## Not in this version

- Typed chat (five canned taunts instead)
- Pictures of their own for the 22 newest cards
- Counter-offers on a trade (it's take it or leave it)
- More than one table on the server
- Phone layout. It's a computer game: mouse and keyboard, 1280x720 or bigger.
