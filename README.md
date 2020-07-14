# Game Swap

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description
An platform for users to coordinate trades between board games/puzzles

### App Evaluation

- **Category:** Social
- **Mobile:** Mobile focused
- **Story:** A platform for users to coordinate trades between board games/puzzles that they no longer want to play**
- **Market:** Puzzlers/Board gamers
- **Habit:** Log on whenver they are looking for a new puzzle to do or a game to play or when they want to get rid of a puzzle/game no longer want.
- **Scope:** A small platform to trade with others. Could be expanded in the future to be able to set up and host events.

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* Users can create an account and log in after creating one
   * Log in persists upon app closure
* Users can create posts with the title, image, condition, and related notes of game/puzzle they are putting up for trade.
* Users can see other users' posts in list view
* Users can see other users' posts in map view (Google Map SDK)
* Users can search for specific posts by title or location
* Users can tap on specific posts to see details of that post
* Users can go from detail view to start a chat with other users to coordinate a meet-up
* Users have public profiles that list all of their posts
* Users can delete their posts

**Stretch Stories**

* Users can post requests
* Users can add more information about the game (description, players, genre)
* Users can filter section in main Stream where can view by request
* Users can favorite to view later
* Users can get hints and autocompletion for title/information from BGG API
* Users can set up public events to trade/play
* Users can block/report users and posts
* Users has a settings page to manage search range/blocklist/notifications

### 2. Screen Archetypes

* Login
    * Users can log in after they have created an account
* Register
    * Users can create an account
* Stream
    * Users can see other users' posts in list view
    * Users can search for specific posts by title or location
* Detail
    * Users can tap on specific posts to see details of
    * Users can favorite stories that post
* Creation
    * Users can create posts with the title, image, description, condition of game/puzzle.
* Profile
    * Users have public profiles that list all of their posts
    * Add ability for users to block/report users
* Settings
   * User has a settings page to manage search range/blocklist/notifications
* Maps
    * Users can see other users' posts in map view
    * Users can search for specific posts by title or location

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Stream
* Map
* Create
* Chats
* Profile

**Flow Navigation** (Screen to Screen)

* Stream
   * Detail
* Map
   * Detail
* Create
* Chats
    * Chat Conversation
* Profile
    * Settings

## Wireframes

[<img src="https://github.com/dprado981/SimpleTweet/blob/master/Wireframes.png">](https://www.figma.com/embed?embed_host=share&url=https%3A%2F%2Fwww.figma.com%2Ffile%2FZ2XG1wFQoGGvQyVRL4XE5K%2FGame-Swap%3Fnode-id%3D0%253A1&chrome=DOCUMENTATION)

## Schema 

### Models
#### Post

   | Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for the user post (default field) |
   | user          | Pointer to User| post user |
   | image         | File     | image that user posts |
   | title         | String   | title of game description by user |
   | notes         | String   | additional info user adds |
   | condition     | Number   | condition rating of game (out of 10) |
   | createdAt     | DateTime | date when post is created (default field) |
   | updatedAt     | DateTime | date when post is last updated (default field) |
   
### Networking
- Create: POST request to add a post (Stretch: GET request to get autofill hints from BGG)
- Stream/Map: GET request to get list of post
- Profile: POST request to setup profile image
- Chat: POST and GET requests to Parse to get past messages and to send new ones