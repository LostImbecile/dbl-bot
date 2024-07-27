# Egubot (DBLegends - Discord Bot)

Discord bot I've made for a specific server. Its features mostly have to do with a mobile game called "Dragon Ball Legends", but there are some other useful things.

Public so server members can look at it or add to it.

Uses [Javacord](https://github.com/Javacord/Javacord).

Notes:
---------------------------------
Some of the classes are implementation specific, you might want to change some as needed, the rest should work as is, but you should modify their messages, clear them up, completely remove or add more to them.

You want to have an "IDs.txt" and a "Tokens.txt" file, they'll be automatically created and filled for certain things, 
but you might have to add the keys manually for others, look for KeyManager references to find the ones I used for my bot.

This bot was made for use on single servers and stores its data on discord or locally. It isn't yet fully made to handle cases where you're running it with multiple servers (storage is sometimes shared), to keep things simple.

The bot comes with a GUI, you can run it as CMD only if you change it in settings and restart, the main class is "Run.java".
 
Compiled with java 17 but can be very easily made to run with java 11. Any older is not recommended as javacord is switching 
to java 11 soon.

Also works on Linux (tested), but resource locations behave differently, isn't a problem however.

Bot commands:
---------------------------------

*Everything is case insensitive. Invocation has to be at the beginning of your message, not read otherwise.*

*Round Brackets indicate operand, stars mean optional, square brackets for comments, everything else needs to be there.*

DBLegends Random Roll:
- b-roll(n) *(filters)
- b-template create (name) (filters)
- b-template remove (name)
- b-template send
- b-toggle roll animation

DBLegends Character Search:
- b-search \*(name) *(filters)
- b-character send
- b-tag send

DBLegends Summon Rate:
- b-summon (banner_url)

Automatic Responses:

 `[Types: contain, equal, match]`
- b-response create (type) >> (msg) >> (response) or (op1 ?? op2 ?? op3 ...) >> \*(reaction) >> *(reaction)...
- b-response remove (message)

Translate:
- b-translate toggle `[Translates all non-eng messages]`
- b-translate set (to)/(from-to) `[Shortened form; en, fr]`
- b-translate languages
- b-translate (text)/(reply to the message)/(embed)

Weather:
- b-weather (city) *detailed

Music Player:
- b-play (link)/(ytsearch: keyword)
- b-cancel
- b-pause
- b-resume
- b-skip *(amount)
- b-info `[The playlist's]`
- b-now `[Playing now]`

WebDriver:
- b-insult (person name) >> (reason)
- b-grab (youtube link)
- b-grab mp3 (youtube link)
- b-convert *(gif/vid) `[Type of your attachment]`

OpenAI Model:
- gpt toggle `[it's off by default]`
- gpt (message)
- gpt channel toggle
- gpt clear
- gpt tokens

Groq Model:
- same as gpt but "aa"

Custom AI:
- ai (message)
- ai terminate/activate

Timers: `[Tasks: weather, parrot, verse]`
- b-timer \*every \*(0M0w0d0h0m0s) \*(2024-7-13, 20:00) "(task) \*(arguments)" \*(channels)
- b-timer remove ^^^
- b-timer toggle ^^^
- b-timer send

Bot Control (Owner):
- terminate
- refresh
- b-message edit (messageId) (message)
- b-message delete (messageId)
- parrot (msg)
- b-toggle manager `[Toggles storage]`
- toggle bot read mode `[Toggles reading bot message]`

### Active
**1) b-roll(n) (filters)** 
- n is a 1 digit number, max of 6 characters are rolled, which is also the default.
- This gets all units and tags from the DBlegends website and rolls n random characters from the pool it creates.
- If you don't specify a filter it pulls from a pool of all units in the game.
- You can control the pool by passing tags to filter it out.
- You can use any brackets, +, - and &.
- &  is there by default, you don't need to type it.
- In case the tag doesn't exist it's ignored, and in case the equation you wrote is wrong, an error message will be sent.
- Names are links to the characters' dblegends.net page.
- Disable or enable roll animation by typing that.

Example:

b-roll6 sparking old + (zenkai - extreme) & blu

=> Pool is all old (year1 + year2) sparking characters, as well as all the blue non-extreme zenkais
‏‏‎

**2) b-template create (name) (filters)**
- This creates a new template that you can substitute as a tag or set of tags in b-roll or b-search.
- Brackets surrounding it are added automatically.
- Template name can't be a tag or another template, and has to be one word.
- Template must contain real tags or other templates.

Examples:

b-template create t1 sparking old + (zenkai - extreme) blu

b-roll6 t1

=> Equivalent to the example in #1 with added brackets at the start and end

b-template create t2 LoE + ginyu_force

where LoE is a template, ginyu force is a tag


**3) b-template remove (name)**
- Removes an existing template if there is one with the same name (case insensitive).
- Default templates can't be removed. You'll be told they don't exist if you try to.

**4) b-template send**
- Sends a file with all templates.

**5) gpt (message)**
- Gets response from chatgpt.
- Conversation is saved up to 3300 tokens, where it starts getting deleted (non-channel specific).
- Use "gpt activate channel" to keep gpt active for current channel without using invocation.
- Use "gpt deactivate" to go back to using invocation.

**6) b-response create (type) >> (msg) >> (response) >> (reaction) >> (reaction)...**
- Adds a new automatic response (whether message or reaction).
- Reactions are optional, there's no limit for their use.
- Standard (unicode) emojis can't be directly used. If you want to use them in a message, do :&lt;thumbsup&gt;:.
- Unicode emojis can't be used in reactions.
- There are 3 types: match, equal & contain.
- "Contain" doesn't trigger if invocation is part of a word or is connected to one.
- Regex can be used for "match" and "contain" (you can type it as is to remove it, or anything that it matches).
- For a random response use "??" to separate options.
Example:

b-response create equal >> test >> :&lt;ok_hand&gt;: >> <:emoji:1142482242589950083>

b-response create contain >> test >> op1 ?? op2 ?? op3

=> Responds with op1 or op2 or op3

**7) b-response remove (message)**
- Removes an automatic response.
- You need to know the exact invocation message unless you get special rights.
- Default responses can't be removed unless you're me.

**8) b-search (name/game_id) (filters)**
- Looks for characters that match the specified name, game ID or filters.
- Underscores must be entered instead of spaces for names.
- If no name is found, it's ignored and filters are checked.
- It's recommended to use the character's main name as a filter.
- Names are links to the characters' <https://dblegends.net/> page.
- Game IDs don't need to have - or "DBL" in them, but can have them.
- You can use -1 in place of a name to only check the filters.
- Search sees if each word in the name is inside the one being checked. 
- Order in names doesn't matter due to the above.
- Max of 10 embeds are shown per page, no page limit, however, you
only have 15 minutes to navigate pages.

Examples:

b-search goku_ssb goku ssb future melee red

=> Finds characters that match the above filters and name


b-search -1 goku vegeta

=> Finds tag units of goku and vegeta


b-search goku_vegeta

=> Equivalent to previous


b-search DBL41-01S

=> Finds ssb goku and vegeta


b-search 21

=> Finds all android 21s

**9) b-character send**
- Like #4 but a file with all characters instead.
- Prints the ID of the unit as well.

**10) b-tag send**
- Like #4 but a file with all tags instead.
- Prints the number of units with the tag.

**11) b-grab (link)**
- Sends a link of the video file to download it locally.
- Accepts youtube links.
- Instagram, tiktok, and twitter may be added.

**12) b-grab mp3 (link)**
- Sends a link of the audio to download it locally.
- Accepts youtube links.

**13) b-convert \*(gif/video)**
- Converts gifs to videos and back.
- Tries to automatically detect which is which, but appending the type is recommended.
- Checks embeds and attachments, if the video/gif doesn't embed, or takes long to, it's ignored.
- You can reply to the message that contains the file, or it can check yours.

**14) b-weather (city) \*detailed**
- Sends an embed with info on the city's weather for today and tomorrow.
- If "detailed" is added, it includes more info and after tomorrow.

**15) b-translate (text)/(reply to the message)/(embed)**
- Translates the text in your message, the reply, or the embed in your message or the reply
- Automatically detects language and translates to English unless set.
- b-translate set (to/from-to) to manually set the language.
- Uses an AI no better than google translate, with the same weaknesses.
- Language is in its shortened form (i.e, en for English, fr for French).
- Do b-translate languages to get all languages and their shortened forms

**16) b-summon (banner_url)**
- Calculates the rates to get the important characters of the banner.
- Also calculates total rates and the cost.
- Calculates amount of rotations needed to have an 80% to get the character.
- Includes chance to get the character to red 2.
- Includes one rotation, three rotations, and 80% chance worth of rotations.
- Get the banner url from <https://dblegends.net/>.

---------------------------------
### TBA 2029:

**17) b-tag create (name) (characters)**
- Creates a new tag that has the characters specified.
- You don't need to specify characters on creation.
- Tag name must be unique and devoid of spaces.
- Tag name must be something that won't be added in the future, 
or the tag will be ignored when it does.
- For characters, paste their dblegends.net link or site ID.

Example:

b-tag create cancer_units <https://dblegends.net/character.php?id=238> 191 491

Creates a tag called cancer_units that contains characters with the above site IDs.

**18) b-tag remove (name)**
- Removes custom tags, default ones can't be removed.

**19) b-tag update (name) (characters)**
- Adds or removes characters from a custom tag.
- Default tags can't be modified.
- Prefix the name with + or -.

---------------------------------
### Inactive:
**20) ai**
- Personal GPT2 AI, generates text randomly when called.
- Has to be running on my end, which is almost never.
- Pretty shit, can be racist and an arsehole as well.