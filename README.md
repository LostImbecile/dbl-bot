# egubot

Discord bot I've made for a specific server.

Bot code:
---------------------------------
The classes in egubot.main are implementation specific, you'll want to change some of them as needed (the
MessageCreateEventHandler in particular), the rest should work as is, but you might have to modify their messages or add more
to them to fit your use case.

You want to have an "IDs.txt" and a "Tokens.txt" file, they'll be automatically created for certain things, but you might
have to add the keys manually for others, look for KeyManager references to find the ones I used for my bot.

Compiled with java 17 but can be easily made to run with java 11.

Bot commands:
---------------------------------
Notes: 

*Everything is case insensitive. Invocation has to be at the beginning of your message, not read otherwise.*

*Brackets indicate operand*

- b-roll(n) *(filters)
- b-template create (name) (filters)
- b-template remove (name)
- b-template send
- b-response create (type) >> (msg) >> (response) >> \*(reaction) >> *(reaction)...
- b-response remove (message)
- b-search \*(name) *(filters)
- b-tag send
- b-tag create (name) *(characters)
- b-tag update (name) (characters)
- b-character send
- gpt (message)
- gpt activate channel
- gpt deactivate
- ai
- ai terminate
- disable roll animation
- enable roll animation

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

=> Pool is all old sparking characters (year1 + year2), as well as all the blue non-extreme zenkais
‏‏‎

**2) b-template create (name) (filters)**
- This creates a new template that you can substitute as a tag or set of tags in b-roll.
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
- Past 30 messages in the server are read (non-channel specific).
- Extremely unstable, if it breaks bot needs to be restarted.
- Use gpt activate channel to keep gpt active for current channel without using invocation.
- Use gpt deactivate to go back to using invocation.

**6) b-response create (type) >> (msg) >> (response) >> (reaction) >> (reaction)...**
- Adds a new automatic response (whether message or reaction).
- Reactions are optional, there's no limit for their use.
- Standard (unicode) emojis can't be directly used. If you want to use them in a message, do :&lt;thumbsup&gt;:.
- Unicode emojis can't be used in reactions.
- There are 3 types: match, equal & contain.
- Contain doesn't trigger if invocation is part of a word or is connected to one.


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
only have a set amount of time to review the results.

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

---------------------------------
### TBA 2029:

**11) b-tag create (name) (characters)**
- Creates a new tag that has the characters specified.
- You don't need to specify characters on creation.
- Tag name must be unique and devoid of spaces.
- Tag name must be something that won't be added in the future, 
or the tag will be ignored when it does.
- For characters, paste their dblegends.net link or site ID.

Example:

b-tag create cancer_units <https://dblegends.net/character.php?id=238> 191 491

Creates a tag called cancer_units that contains characters with the above site IDs.

**12) b-tag remove (name)**
- Removes custom tags, default ones can't be removed.

**13) b-tag update (name) (characters)**
- Adds or removes characters from a custom tag.
- Default tags can't be modified.
- Prefix the name with + or -.

---------------------------------
### Inactive:
**14) ai**
- Personal GPT2 AI, generates text randomly when called.
- Has to be running on my end, which is almost never.
- Pretty shit, can be racist and an arsehole as well.