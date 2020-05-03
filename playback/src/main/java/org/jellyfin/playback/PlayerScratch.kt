package org.jellyfin.playback

// This file is used to design the overall architecture of the playback module
// all code in this file should be moved to separate files when used

interface Player

class LibVLCPlayer : Player
class ExoPlayerPlayer : Player
class ExternalPlayer
