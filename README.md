# Message Editor 
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT) ![build](https://github.com/jaqobb/message-editor/workflows/build/badge.svg)

Message Editor is a Spigot plugin that allows editing in-game messages that were previously unmodifiable, in easy and fast way.

### Features

Message Editor supports:
* [x] Editing chat messages - since 1.0.0
* [x] Editing action bar messages - since 1.0.0
* [ ] Editing boss bar titles
* [ ] Editing scoreboard titles
* [ ] Editing scoreboard entries
* [ ] Triggering message editing only on specific positions
* [ ] Changing message positions

### Showcase

![](images/showcase_before.png)

Join message before editing it using Message Editor.

![](images/showcase_after.png)

Join message after editing it using Message Editor.

### Usage

While you are setting up the plugin, only administration should be allowed to join the server.

After the config.yml file has been generated, set `log-messages` to `true` or if you are at least on 1.15, set `log-messages` to `false` and `attach-special-hover-and-click-events` to `true`.

`log-messages` set to `true` will log every message's JSON to the console, so that you can copy it and then edit to meet your needs.

`attach-special-hover-and-click-events` set to `true` will attach special hover and click events to every chat message, so you can just click on that message, and the message's JSON will be copied to your clipboard.

Only one of those options should be enabled at any given time. Special hover and click events require your server version to be at least 1.15, and will be automatically disabled if copying to clipboard is not supported on your server (that is your server version is below 1.15).

Let us try to edit a message on a 1.16.1 server. We are gonna edit the /version message, "You are x version(s) behind" part to be more precise.

After running /version command and hovering over the message you want to edit you should see a hover message:

![](images/usage_before.png)

Now you can click this message, and you will have this message's JSON copied to your clipboard.

In this case you should end up with this JSON:

`\{"extra":\[\{"text":"You are 1 version\(s\) behind"\}\],"text":""\}`

You should see the same JSON in your console if you are using `log-messages` setting instead of `attach-special-hover-and-click-events`. In that case hovering and clicking is not required and you just have to check the console for the message's JSON.

This JSON is already ready for additional regex. If your message is static then you are already good to go and edit the message. In this case we are not ready since we can be behind more than 1 version.

We need to make the message more generalized, and we are gonna use regex to achieve that. If you do not know regex then you are probably gonna find this plugin useless.

We can replace `1` with `(\d+)`. This makes it accept all digits and not only `1`. This now also changed it to a sort of variable that we will be able to use in the new message.

This makes our JSON look like that:

`\{"extra":\[\{"text":"You are (\d+) version\(s\) behind"\}\],"text":""\}`

Now you can go and create your new message using i.e. special website.

I have created a new message that looks like that:

`{"extra":[{"color":"green","text":"You are $1 version(s) behind, consider updating."}],"text":""}`

Since we captured the number of versions we are behind, we can now also use it in the new message.

We are pretty much done. The only thing left is adding all we have done to the config.yml file, so it looks like that in the end:
```yml
log-messages: false

attach-special-hover-and-click-events: true

message-edits:
- ==: MessageEdit
  message-before-pattern: '\{"extra":\[\{"text":"You are (\d+) version\(s\) behind"\}\],"text":""\}'
  message-after: '{"extra":[{"color":"green","text":"You are $1 version(s) behind. Consider updating."}],"text":""}'
```

`==: MessageEdit` is for serialization purposes only.

`message-before-pattern` is a pattern to make sure we are editing the right message.

`message-after` is a new message you will see.

After this is done you should be able to see the new message after running /version command:

![](images/usage_after.png)

### Requirements

Message Editor requires:
* Server version at least 1.8
* Server version at least 1.15 - if you want to attach special hover and click events
* ProtocolLib (possibly the latest version available)
* PlaceholderAPI (optional) - if you want to add placeholders to new messages
* MVdWPlaceholderAPI (optional) if you want to add placeholders to new messages
