# Egubot (Discord Bot)

Discord bot I've made for a specific server, covers various tasks (90+ commands).

Public so server members can look at it or add to it.

Uses [Javacord](https://github.com/Javacord/Javacord) with [JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html). Works on Windows and Linux. 

Note that Javacord will end its support in 2025 and a refactor to JDK could be necessary, but the code only relies on basic features of the library and it's rare for discord to introduce breaking changes.

Get your bot token from [here](https://discord.com/developers/applications).

Notes:
---------------------------------
Some of the classes are specific to my needs, you might want to change some to fit yours, the rest should work as is. You can also change the main thread counts in the message handler if I/O (Network) locks them frequently.

You must have an "IDs.txt" and a "Tokens.txt" file, they'll be automatically created and filled for certain things, 
but you might have to add the keys manually for others, look for KeyManager references to find the ones I used for my bot. Missing a token or an ID isn't breaking and will not cause problems.

Data can be stored with SQLite or locally, and you can switch between them during run-time. Most of it is saved per server or channel and is cached in memory when called for specifically. Prefix can be changed in config.properties or the GUI settings.

The bot comes with a GUI, you can run it as CMD only if you change it in settings and restart, the main class is "Run.java".
 
Compiled with java 17 but can be very easily made to run with java 11. Any older is not recommended to maintain compatibility with modern frameworks.

Some API keys used (Optional):
[`OpenAI`](https://beta.openai.com/account/api-keys)
[`Groq`](https://console.groq.com/keys)
[`Youtube`](https://console.developers.google.com/apis/credentials)
[`Azure Translate`](https://docs.microsoft.com/en-us/azure/cognitive-services/translator/)
[`Weather`](https://www.weatherapi.com/)

### Message Flow:

- Messages are received in the "MessageCreateEventHandler"
- The CommandManager checks for prefixes matching any present in classes that implement the "Command" interface
- On a match, the command class processes the text either directly or through a facade that interacts with the database or other services
- If no match, the text is checked for auto-reponses, AI conversation responses, or auto-translate based on what's toggled

### Background Tasks:
These start on run-time and will always be on as long as a timer is active or a newsfeed was subscribed to.
- Newsfeed schedulers
- Timers

Device sleep/shutdown or summer-time transitions are tracked and accounted for, accuracy is maintained across multiple years from my testing.

### Cached Objects:
- Server IDs and Details
- Channel IDs, Parent Servers and Details
- Command specific data per server/channel when called by a user there
- Newsfeed article titles or IDs
- Timers
- Server media players for channels where bot was summoned
- Some websites used for database building/lookup

Cache is updated on a command-call basis and is maintained throughout the lifetime of the bot, no real case where the amount of objects will be too large (even with tens of thousands of active servers), but it's easy to add timeouts/limits if necessary (inside the Facades). Commands that are never called by a server will simply not cache anything for it.

### Expensive Tasks:
Disable or limit them if necessary.
- AI model interactions (rate limits + cost)
- Web Browser automated tasks (Entire browser instance is created)
- Github details fetch (rate limits, slow response times)
- Music Player (Network)
- Translate (rate limits + cost)
- TenorLink fetch (response time)
- DBLegends commands (online search/fetch)

Mostly tasks that rely on APIs or online data they need to fetch when called. Local tasks rely on caches and primitive operations and are processed within a few nanoseconds, so network will be the bottleneck for the most part as local storage is rarely in-need of an update.

Bot commands:
---------------------------------
*Everything is case insensitive. Invocation has to be at the beginning of your message, not read otherwise.*

*Round Brackets indicate operand, stars mean optional, square brackets for comments, everything else needs to be there.*

Moderation:
- b-kick @user1 @user2 ...
- b-ban @user1 @user2 ...
- b-mute @user1 0w0d0h0m0s `[28d max]`
- b-clear amount `[100 recent messages at once, 10 old at once]`

DBLegends Random Roll:
- b-roll(n) \*(filters)
- b-template create (name) (filters)
- b-template remove (name)
- b-template send
- b-toggle roll animation

DBLegends Character Search:
- b-search \*(name) \*(filters)
- b-search kit \*(keywords, comma separated)
- b-character send
- b-tag send

DBLegends Summon Rate:
- b-summon (banner_url)

DBLegends News
- b-news register \*(channel tags) \*(user or role tags)
- b-news remove
- b-news update \*(channel tags) \*(user or role tags)

Financial System:
- b-balance \*(user)
- b-balance short
- b-balance server
- b-loan
- b-loan (user) (amount) \*(0d0h0) \*(deduction from earnings percentage `[deduction: 0.1-1 (10%-100%) & 1h < due date < 2d]`) 
- b-bank loan
- b-bank loan (amount)
- b-bank loan pay (amount/all)
- b-bet (amount/all) (colour) `[red, black, green]`
- b-bet (amount) `[group bet with more incentives but a base bet amount]`
- b-transfer (user) (amount) `[also used to pay back user loans]`
- b-daily
- b-hourly
- b-work
`[Server Owner only]`
- b-daily set (amount)
- b-hourly set (amount)
- b-transfer set (amount)
- b-balance set (amount)
- b-balance set (user) (amount)
- b-bank loan reset *\(user)
- b-loan reset *\(user)

Message Highlights:
- b-highlight
- b-highlight toggle
- b-highlight (emoji) (channel) (threshold `[1-50]`)

Automatic Responses:

 `[Types: contain, equal, match]`
- b-response create (type) >> (msg) >> (response) or (op1 ?? op2 ?? op3 ...) >> \*(reaction) >> \*(reaction)...
- b-response remove (message)
- b-reponse edit (message) >> \*(response:newResponse) >>\*(attr:attr=value) >> \*(blacklist/whitelist:channelTag,userTag) >> \*(reactions:reaction) `[Commas for separation]`
- b-response toggle `[toggle automatic responses/deletions]`

Automatic Delete:
- b-delete msg >> (msg)
- b-delete user >> (user tag)
- b-delete remove (msg or user tag)

Translate:
- b-translate toggle `[Translates all non-eng messages]`
- b-translate set (to)/(from-to) `[Shortened form; en, fr]`
- b-translate languages
- b-translate (text)/(reply to the message)/(embed)

Weather:
- b-weather (city) *detailed

Automatic Embed Fix For Popular Sites:
- b-fixembed toggle `[on by default]`
- b-fix (link)/(reply to the message)

Get Media URLs from Message (for download):
- b-dl (reply to the message)/(embed)

Music Player:
- b-play (link)/(ytsearch: keyword)
- b-cancel
- b-pause
- b-resume
- b-skip \*(amount)
- b-info `[The playlist's]`
- b-now `[Playing now]`
- b-buffer (big\small)

WebDriver:
- b-insult (person name) >> (reason)
- b-grab (youtube link)
- b-grab mp3 (youtube link)
- b-convert \*(to vid/to gif) or \*(gif/vid) `[Type of your attachment]`

Github:
- b-github (file or repo link) `[returns the raw file or readme]`
- b-github list (file or repo link) `[returns the file structure, subject to rate limits]`

OpenAI Model:
- gpt toggle
- gpt (message)
- gpt channel toggle
- gpt clear
- gpt tokens
- gpt change model (model) `[owner]`
- gpt model `[returns model name]`
- gpt list `[lists models]`

Groq Model:
- same as gpt but "aa"

Ollama Model:
- same but "qq"

Gemini Model:
- same but "gem" `[image support]`
- b-vision (text prompt) (reply to the message)/(embed)

System Prompt Management:
- sys toggle `[Toggles between system and user message delivery]`
- sys get `[View current server's system prompt]`
- sys set (prompt) `[Set custom system prompt for server - Admin only]`
- sys reset `[Reset server's prompt to default - Admin only]`
- sys default get `[View global default prompt - Owner only]`
- sys default set (prompt) `[Change global default prompt - Owner only]`
- sys default reload `[Reload default from file - Owner only]`

Timers: `[Tasks: weather, parrot, verse]`
- b-timer \*every \*(0M0w0d0h0m0s) \*(2024-7-13, 20:00) "(task) \*(arguments)" \*(send on miss/terminate on miss) \*(channels)
- b-timer remove ^^^
- b-timer toggle ^^^
- b-timer send
- b-remindme \*every \*(0M0w0d0h0m0s) \*(2024-7-13, 20:00) "(message)" \*(send on miss/terminate on miss) \*(channels)
- b-remindme cancel (0M0w0d0h0m0s) \*(channels) `[For dates the delay is 1s]`

Notes (Owner):
- b-note (your note)
- b-note remove (note id)
- b-note send

Bot Control (Owner):
- terminate
- refresh
- restart
- parrot (msg)
- ping
- toggle bot read mode `[Toggles reading bot message]`
- b-message edit (messageId) (message)
- b-message delete (messageId)
- b-toggle manager `[Toggles storage]`