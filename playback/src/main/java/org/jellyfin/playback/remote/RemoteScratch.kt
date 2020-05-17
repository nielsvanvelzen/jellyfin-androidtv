package org.jellyfin.playback.remote

// This file is used to design the overall architecture of the playback module
// all code in this file should be moved to separate files when used

// Jellyfin websocket
class JellyfinSocketRemote : Remote

// The remote in the user interface
class UIRemote : Remote

// The physical remote
class PhysicalRemote : Remote
