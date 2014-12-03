/*
Interactive/Responsive Looping and Beat Generation Interface.
Creates the GUI for users to mimic their real world actions with the hardware electronic pads to the graphic.
Uses simple 'key presses' with the external pads via a 'Makey Makey', of which instructions are in main project folder.

Part of the Interactive/Responsive Looping and Beat Generation Interface.
(C) 2014-2015, Candidate Number 19329
Advanced Computer Music, University of Sussex, Spring 2014.
*/
a = InteractiveLoopingInterface();


// Quick shortcut to record experiiments
s = Server.local
s.record
s.stop

// 1. Ensure Microphone is plugged in and server recognises it.
// 2. If not connected via the Makey Makey, feel free to use the key commands listed on the GUI

// Video Example
// "http://www.youtube.com/watch?v=xbXEqcBfqZA&feature=youtu.be"

// Sound Example
// "https://soundcloud.com/jamesnapierstuart/interactive-responsive-loop"