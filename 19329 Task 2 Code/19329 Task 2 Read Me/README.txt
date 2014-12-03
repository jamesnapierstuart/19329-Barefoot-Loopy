"Interactive and Responsive Looping and Beat Generation Interface"
CANDIDATE NUMBER: 19329
Advanced Computer Music: G6003
Task 2
University of Sussex
January 2014

BUILT ON SUPERCOLLIDER 3.6.5
MAC OSX VERSION 10.8.4

Interactive and Responsive Looping and Beat Generation Interface 1.0

Welcome, before you get started if you are using the electronic hardware pads for this interface, make sure to read the set up instructions first. 
Please note it is still possible to use the interface without the pads by using key presses as highlighted in the Graphical User Interface above each pad.

** Set Up Instructions:

Setting up the interactive system is very straightforward.
Take the Makey Makey Arduino board and connect the USB to the computer USB port.
No code or installation process is needed (if a keyboard assistant client opens ignore it).
The Makey Makey acts just as an ordinary keyboard, except we can connect wires to the points of its circuit.

So all that needs to be done is connect the wires from the pads to the corresponding key inputs.
If you go ahead and launch the GUI interface it gives a perfect map of this but here it is anyway.

w	s	d

f		g

up	left	right

So use this mapping to connect the pads to the correct key inputs on the Makey Makey device. 
The last thing, is to connect the ground to each of the pads by threading wire through the pads and under the middle foil piece.
Then connect these to the 'earth' input of the Makey Makey.
Thats it all done! :) test by making sure feet cover both foil parts when pressing a button to bridge the connection between the key press and ground. 

Now you're ready to use the interface have fun (and there's some more tips below).

** Starting the system:

- First create a new InteraciveLoopingInterface by calling the .new method or just ();
- The GUI appears and you're ready to get going.

Using the system:

- Each electronic pad corresponds to a pad on the GUI in the same orientation.
- By pressing upon the foil covered pad, you bridge a connection in the circuit effectively closing the circuit which causes the pads to trigger an action.
- The labels on screen show the user which pads do which functions.
- All top 3 pads always stay the same, 'dry input', 'mode switcher' and 'click on/off'.
- Switch through the modes to change the type of input and response you get with the system. 
- It is split into 3 main modes outline next.

Please note: Some pads such as click and mutes can be left on throughout changing modes.
However, the majority of pads must be de-activated before another pad is used, to help avoid misunderstanding and mistakes.

** The 3 modes:

1. Looping = Live input is taken and whole bar loops are made from it.
Multiple loops can be recorded.
All of them cleared.
The last one cleared as well, if you didn't quite play in time
It also features a simple form of responsive AI that plays back the pitches which the user plays in a very slow order.
The input is gated so no noise travels in, and the user can stop the responses and loops too.

2. Beat Generation = Making beat patterns of kick, snare, hihat and toms from using onset detection played in by the live input of user.
The audio is once again tailored to conform to whole bar loops but it goes a step beyond the live looping mode by keeping track of where you started recording.
E.g. If listening to a loop and start recording form the middle of the bar, it will remember this and trigger from the middle of the bar instead of at the start of every bar.
Another exciting feature is that it quantises the audio you play in, in order to create beats. User plays freely into the input and the system will analyses this data and quantise it to make beat patterns in the realm of 1/16th notes.
The AI response is also more developed in this mode, using Key detection from the live loop input of the user it chooses to play notes from the key and scale (also determined by user input).
When defining kick patterns the system will react with bass lines on the hits of the kick, with hihats the system will play notes of the scale wherever the hats don't play. And the interactions go on.

3. Mute/Stop Control = Mute/Stop control mode, is where the user can choose to mute the various aspects of the output. For example mute everything, mute the AI response, mute the live loops or mute the beats and any combination of these.
Very useful when wanting silence in the piece of just to pull out various elements especially in conjunction with the dry input pad for improvisation. Here you can also do a full stop of everything in the system.


** Video Demo:
http://www.youtube.com/watch?v=xbXEqcBfqZA&feature=youtu.be

** Sound Example:
https://soundcloud.com/jamesnapierstuart/interactive-responsive-loop



