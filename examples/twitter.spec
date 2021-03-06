//
// Source https://dev.twitter.com/docs/api/1.1
//

type Error = {}
type Nothing = {}

//------------------------------------------------------------------------------------------
// OAuth2 service
//------------------------------------------------------------------------------------------

type Token = {
    token_type: string,
    access_token: string
}

type Credentials = {
    virtual Authorization = [<token_type> <access_token>]
}

service OAuth2 [oauth2/token] {
    authenticate: Token => Token = POST HEADER[Credentials]
}

//------------------------------------------------------------------------------------------
// Timeline service
//------------------------------------------------------------------------------------------

type Timeline = {
    count: int,
    since_id: string,
    max_id: string,
    trim_user: bool
}

type MentionsTimeline = Timeline with {
    contributor_details: bool,
    include_entities: bool
}

type UserTimeline = Timeline with {
    user_id: string?,
    screen_name: string?,
    exclude_replies: string?,
    contributor_details: bool,
    include_rts: string?
}

type HomeTimeline = Timeline with {
    contributor_details: bool,
    exclude_replies: string?
}

type RetweetsOfMe = Timeline with {
    include_entities: bool,
    include_user_entities: bool
}

service timelines(Token) [1.1/statuses] {
    mentions_timeline: MentionsTimeline => Nothing = GET[mentions_timeline.json] HEADER[Credentials] PARAMS[MentionsTimeline]
    user_timeline    : UserTimeline     => Nothing = GET[user_timeline.json]     HEADER[Credentials] PARAMS[UserTimeline]
    home_timeline    : HomeTimeline     => Nothing = GET[home_timeline.json]     HEADER[Credentials] PARAMS[HomeTimeline]
    retweets_of_me   : RetweetsOfMe     => Nothing = GET[retweets_of_me.json]    HEADER[Credentials] PARAMS[RetweetsOfMe]
}

client twitterRest provides timelines
