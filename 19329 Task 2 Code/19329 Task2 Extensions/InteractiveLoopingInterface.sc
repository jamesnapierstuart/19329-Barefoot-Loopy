/*
Interactive Looping Interface Class:
Creates the graphical User interface to show the user the functions of the outboard pads.
Implements live looping, AI responder and beat generation from onset detection information and pitch input.
Three different states are available, 1. Looping, 2. Beat Generation, 3. Mute Control.
All three aspects of the interface are controlled withn each state and the mute mode to give user more freedom.
Click track is ON/OFF, dry input and mode switcher are all concrete functions,
Whereas all other pads are flexible and change their function depending on the mode you are in.

Part of the Interactive/Responsive Looping and Beat Generation Interface.
(C) 2014-2015, Candidate Number 19329
Advanced Computer Music, University of Sussex, Spring 2014.
*/

InteractiveLoopingInterface {

	*new {
		^super.newCopyArgs.create();
	}

	create {
		var w, v, titlecv, titletext, s;
		var font, font2, font3;
		var modeText, modeSelection;
		var environment;
		var topbuttons, midbuttons, lowbuttons;
		var freeSpaceCv;
		var loopingMode, beatGenerationMode, muteMode;
		var keyFunc;
		var key = 0;
		var mode = 0;
		var keyOnOff = Array.fill(8, {arg i; false});
		var checkFunc;
		var click = false;

		// Mute cases
		var muteAll = false;
		var muteResponse = false;
		var muteBeats = false;
		var muteLoops = false;

		// Functions
		var setGUI;
		var loadSynths;
		var assignSynths;

		// Include Classes
		var createSynths;
		var dryInputSynth;
		var tempo, tempoSynth;
		var tempoPattern;

		// currentbarNo and beatStart for working out where to start the beats from
		var currentBarNo = 0;
		var beatStart = 0;
		// Time a which person punched in to record
		var recordTime;

		// Import beatgen and loopGen classes
		var beatGeneration = BeatGeneration.new();
		var liveLoopGeneration = LiveLoopGeneration.new();
		var intelligentResponse = IntelligentResponse.new();

		// key text
		var toptext;
		var midtext;
		var lowtext;

		// Tempo assignment
		loadSynths = {
			createSynths = CreateSynths.new();
			createSynths.create;

			tempo = TempoClock.new(2, 0, Main.elapsedTime.ceil);
			// beatGeneration.setTempo(tempo);
			beatGeneration.detect;
			liveLoopGeneration.detect;
			intelligentResponse.setTempo;
		};

		assignSynths = {

			tempoPattern = Pbind(
				\instrument, \blip,
				\amp, 0.2,
				\freq, Pseq([Pseq([400, 200, 200, 200], inf)]),
				\freq, Pseq([Pseq([400, 200, 200, 200], inf)]),
				\level, Pseq([Pseq([1, 1, 1, 1], inf)]),
				\dur, Pseq([Pseq([1, 1, 1, 1], inf)]),
			).play(clock: tempo, quant: 4);

			tempoPattern.mute;

			fork {
				tempo.schedAbs(tempo.beats.round(0.25), {
					arg beat, sec;
					case
					{beat%4 == 0} {
						currentBarNo = beat;
					}
					{beat%4 != 0} {
					};
					0.25
				});
			};

			dryInputSynth = Synth(\dryInput);
			dryInputSynth.set(\amp, 0);
		};

		setGUI = {
			// Fonts
			font = Font("Chalkboard", 20, true);
			font2 = Font("Chalkboard", 16, true);
			font3 = Font("Chalkboard", 16);

			// Window, userviews, composite views
			w = Window("Interactive/Generative System", Rect(350, 200, 1000, 600));
			v = UserView(w, Rect(0, 0, 1000, 600));
			v.background = Color.white;
			titlecv = CompositeView(v, Rect(300, 0, 500, 60));
			titlecv.background = Color.white(0.6);
			titletext = StaticText(titlecv, Rect(75, -10, 500, 80));
			titletext.string = "INTERACTIVE/GENERATIVE SYSTEM";
			titletext.font_(font);
			titletext.stringColor = Color.black;

			// Static Texts
			modeText = Array.fill(3, {arg i; StaticText(v, Rect(50, 100 + (i*51), 100, 50))});
			modeText.do{arg i;
				i.background = Color.grey;
				i.font_(font3);
				i.stringColor = Color.white;
			};
			modeText[0].string = "  Live Loops";
			modeText[1].string = "  Beat Generation";
			modeText[2].string = "  Mute/Stop Control";

			modeSelection = StaticText(v, Rect(50, 50, 100, 50));
			modeSelection.string = "    Mode Selection:";
			modeSelection.font_(font2);
			modeSelection.stringColor = Color.black;

			environment = CompositeView(v, Rect(180, 100, 740, 440));
			environment.background = Color.white(0.7);

			freeSpaceCv = CompositeView(environment, Rect(280, 160, 200, 120));
			freeSpaceCv.background = Color.grey(0.2);

			topbuttons = Array.fill(3, {arg i; StaticText(v, Rect(250 + (i*250), 120, 120, 80))});
			toptext = Array.fill(3, {arg i; StaticText(v, Rect(250 + (i*250), 75, 120, 40))});
			midbuttons = Array.fill(3, {arg i; StaticText(v, Rect(250 + (i*250), 280, 120, 80))});
			midtext = Array.fill(3, {arg i; StaticText(v, Rect(250 + (i*250), 235, 120, 40))});
			lowbuttons = Array.fill(3, {arg i; StaticText(v, Rect(250 + (i*250), 440, 120, 80))});
			lowtext = Array.fill(3, {arg i; StaticText(v, Rect(250 + (i*250), 395, 120, 40))});

			// Generative Mode Graphics
			loopingMode = {
				modeText.do{arg i;
					i.background = Color.grey;
				};
				modeText[0].background = Color.green(0.6, 0.8);

				topbuttons.do{arg i;
					i.background = Color.grey;
					i.font_(font3);
					i.stringColor = Color.white;
				};

				midbuttons.do{arg i;
					i.background = Color.grey;
					i.font_(font3);
					i.stringColor = Color.white;
				};
				lowbuttons.do{arg i;
					i.background = Color.grey;
					i.font_(font3);
					i.stringColor = Color.white;
				};

				topbuttons[1].background = Color.green(0.6, 0.8);
				case
				{click == false} {
					topbuttons[2].background = Color.grey;
				}
				{click == true} {
					topbuttons[2].background = Color.red;
				};

				topbuttons[0].string = "   Dry Input";
				topbuttons[1].string = "   Mode Switcher";
				topbuttons[2].string = "   Cick On/Off";

				midbuttons[0].string = "   Stop Loops & Loop Response";
				midbuttons[1].string = "";
				midbuttons[2].string = "   Loop Input";

				lowbuttons[0].string = "   Clear Last Loop";
				lowbuttons[1].string = "   Clear All Loops";
				lowbuttons[2].string = "   ";

				toptext.do{arg i;
					i.background = Color.white;
					i.font_(font3);
					i.stringColor = Color.black;
				};

				midtext.do{arg i;
					i.background = Color.white;
					i.font_(font3);
					i.stringColor = Color.black;
				};
				midtext[1].visible = false;

				lowtext.do{arg i;
					i.background = Color.white;
					i.font_(font3);
					i.stringColor = Color.black;
				};

				toptext[0].string = "    (Key: w)";
				toptext[1].string = "    (Key: s)";
				toptext[2].string = "    (Key: d)";

				midtext[0].string = "    (Key: f)";
				midtext[2].string = "    (Key: g)";

				lowtext[0].string = "    (Key: up)";
				lowtext[1].string = "    (Key: left)";
				lowtext[2].string = "    (Key: right)";

			};

			// Call and Response Mode Graphics
			beatGenerationMode = {
				modeText.do{arg i;
					i.background = Color.grey;
				};
				modeText[1].background = Color.blue(1, 0.7);

				topbuttons.do{arg i;
					i.background = Color.grey;
					i.font_(font3);
					i.stringColor = Color.white;
				};
				midbuttons.do{arg i;
					i.background = Color.grey;
					i.font_(font3);
					i.stringColor = Color.white;
				};
				lowbuttons.do{arg i;
					i.background = Color.grey;
					i.font_(font3);
					i.stringColor = Color.white;
				};

				topbuttons[1].background = Color.blue(1, 0.7);
				case
				{click == false} {
					topbuttons[2].background = Color.grey;
				}
				{click == true} {
					topbuttons[2].background = Color.red;
				};

				topbuttons[0].string = "   Dry Input";
				topbuttons[1].string = "   Mode Switcher";
				topbuttons[2].string = "   Cick On/Off";

				midbuttons[0].string = "   Stop Beats & Beat Response";
				midbuttons[1].string = "";
				midbuttons[2].string = "   Tom";

				lowbuttons[0].string = "   Kick";
				lowbuttons[1].string = "   Snare";
				lowbuttons[2].string = "   Hat";

				toptext[0].string = "    (Key: w)";
				toptext[1].string = "    (Key: s)";
				toptext[2].string = "    (Key: d)";

				midtext[0].string = "    (Key: f)";
				midtext[2].string = "    (Key: g)";

				lowtext[0].string = "    (Key: up)";
				lowtext[1].string = "    (Key: left)";
				lowtext[2].string = "    (Key: right)";
			};

			// Synthesis Mode Graphics
			muteMode = {
				modeText.do{arg i;
					i.background = Color.grey;
				};
				modeText[2].background = Color.magenta(0.7, 0.8);

				topbuttons.do{arg i;
					i.background = Color.grey;
					i.font_(font3);
					i.stringColor = Color.white;
				};
				midbuttons.do{arg i;
					i.background = Color.grey;
					i.font_(font3);
					i.stringColor = Color.white;
				};
				lowbuttons.do{arg i;
					i.background = Color.grey;
					i.font_(font3);
					i.stringColor = Color.white;
				};

				topbuttons[1].background = Color.magenta(0.7, 0.8);
				case
				{click == false} {
					topbuttons[2].background = Color.grey;
				}
				{click == true} {
					topbuttons[2].background = Color.red;
				};

				case
				{muteResponse == false} {
					lowbuttons[2].background = Color.grey;
				}
				{muteResponse == true} {
					lowbuttons[2].background = Color.red;
				};

				case
				{muteAll == false} {
					midbuttons[2].background = Color.grey;
				}
				{muteAll == true} {
					midbuttons[2].background = Color.red;
				};

				case
				{muteBeats == false} {
					lowbuttons[0].background = Color.grey;
				}
				{muteBeats == true} {
					lowbuttons[0].background = Color.red;
				};

				case
				{muteLoops == false} {
					lowbuttons[1].background = Color.grey;
				}
				{muteLoops == true} {
					lowbuttons[1].background = Color.red;
				};


				topbuttons[0].string = "   Dry Input";
				topbuttons[1].string = "   Mode Switcher";
				topbuttons[2].string = "   Cick On/Off";

				midbuttons[0].string = "   Stop All";
				midbuttons[1].string = "";
				midbuttons[2].string = "   Mute All";

				lowbuttons[0].string = "   Mute Beats";
				lowbuttons[1].string = "   Mute Loops";
				lowbuttons[2].string = "   Mute Response";

				toptext[0].string = "    (Key: w)";
				toptext[1].string = "    (Key: s)";
				toptext[2].string = "    (Key: d)";

				midtext[0].string = "    (Key: f)";
				midtext[2].string = "    (Key: g)";

				lowtext[0].string = "    (Key: up)";
				lowtext[1].string = "    (Key: left)";
				lowtext[2].string = "    (Key: right)";
			};

			// Function that evaluates the key presses, for controlling interface
			keyFunc = {
				var checker = 0;
				keyOnOff.do{arg i;
					case {i == false} {checker = checker + 0}
					{i == true} {checker = checker + 1};
				};
				switch(key,
					13, {
						case
						{checker == 0} {
							topbuttons[0].background = Color.red;
							keyOnOff[0] = true;
							dryInputSynth.set(\amp, 0.3);
						}
						{keyOnOff[0] == true} {
							topbuttons[0].background = Color.grey;
							keyOnOff[0] = false;
							dryInputSynth.set(\amp, 0);
						};
					},
					1, {
						case
						{checker == 0} {
							case
							{mode == 0} {
								mode = 1;
								beatGenerationMode.value;
							}
							{mode == 1} {
								mode = 2;
								muteMode.value;
							}
							{mode == 2} {
								mode = 0;
								loopingMode.value;
							}
						}
					},
					2, {
						case
						{click == false} {
							topbuttons[2].background = Color.red;
							click = true;
							// tempoSynth.set(\amp, 0.2);
							tempoPattern.unmute;

						}
						{click == true} {
							topbuttons[2].background = Color.grey;
							click = false;
							// tempoSynth.set(\amp, 0);
							tempoPattern.mute;
						};
					},
					3, {
						case
						{checker == 0} {
							case
							{mode == 0} {

								// Our stop loops and loop generation key here

								midbuttons[0].background = Color.red;
								keyOnOff[3] = true;

								// Stop loops and loop response simultaneoulsy.
								intelligentResponse.stopLoopResponses;
								liveLoopGeneration.stopLoops;



							}
							{mode ==1} {midbuttons[0].background = Color.red;
								keyOnOff[3] = true;
								// Stop beats and beat response
								intelligentResponse.stopBeatResponses;
								beatGeneration.stopBeats;
							}
							{mode ==2} {midbuttons[0].background = Color.red;
								keyOnOff[3] = true;
								// Stop beats and beat response
								intelligentResponse.stopResponses;
								beatGeneration.stopBeats;
								liveLoopGeneration.stopLoops;
							}


						}
						{keyOnOff[3] == true} {
							case
							{mode == 0} {
								midbuttons[0].background = Color.grey;
								keyOnOff[3] = false;
							}
							{mode == 1} {
								midbuttons[0].background = Color.grey;
								keyOnOff[3] = false;
							}
							{mode == 2} {
								midbuttons[0].background = Color.grey;
								keyOnOff[3] = false;
							}
						};
					},
					5, {
						case
						{checker == 0} {
							case
							{mode == 0} {
								midbuttons[2].background = Color.red;
								keyOnOff[4] = true;

								beatStart = (recordTime - currentBarNo);
								"beat Start".postln;
								beatStart.postln;
								liveLoopGeneration.setBeatStart(beatStart);
								liveLoopGeneration.inputFunc();
							}
							{mode == 1} {
								midbuttons[2].background = Color.red;
								keyOnOff[4] = true;
								beatGeneration.setTomIn(true);
								beatStart = (recordTime - currentBarNo);
								"beat Start".postln;
								beatStart.postln;
								beatGeneration.setBeatStart(beatStart);
								beatGeneration.inputFunc();
								intelligentResponse.stopTomResponse;
							}
							{mode == 2} {
								case
								{muteAll == false} {
									midbuttons[2].background = Color.red;
									muteAll = true;
									intelligentResponse.muteResponses;

									beatGeneration.muteBeats;
									liveLoopGeneration.muteLoops;
								}
								{muteAll == true} {
									midbuttons[2].background = Color.grey;
									muteAll = false;
									intelligentResponse.unmuteResponses;
									beatGeneration.unmuteBeats;
									liveLoopGeneration.unmuteLoops;
								};
							}
						}
						{keyOnOff[4] == true} {
							case
							{mode == 0} {
								midbuttons[2].background = Color.grey;
								keyOnOff[4] = false;
								liveLoopGeneration.stopRoutine();
								liveLoopGeneration.findKey();
								intelligentResponse.setKey(liveLoopGeneration.getKey);
								intelligentResponse.setScale(liveLoopGeneration.getScale);
								// Check mutes
								case
								{muteLoops == true} {
									liveLoopGeneration.muteLoops;
								}
								{muteResponse == true} {
									intelligentResponse.muteLoopResponses;
								}
								{muteAll == true} {
									intelligentResponse.muteLoopResponses;
									liveLoopGeneration.muteLoops;
								};

								intelligentResponse.respond();
								intelligentResponse.loopResponse(liveLoopGeneration.getPitchArray);

								case
								{muteResponse == true} {
									intelligentResponse.muteLoopResponses;
								}
								{muteAll == true} {
									intelligentResponse.muteLoopResponses;
								}
							}
							{mode == 1} {
								midbuttons[2].background = Color.grey;
								keyOnOff[4] = false;
								beatGeneration.stopRoutine();
								beatGeneration.findBeat();
								intelligentResponse.respond();
								intelligentResponse.tomResponse(beatGeneration.getLevelArray, beatGeneration.getDurArray, beatStart);
								// Check mutes
								case
								{muteBeats == true} {
									beatGeneration.muteBeats;
								}
								{muteResponse == true} {
									intelligentResponse.muteBeatResponses;
								}
								{muteAll == true} {
									intelligentResponse.muteBeatResponses;
									beatGeneration.muteBeats;
								}
							}
							{mode == 2} {
								midbuttons[2].background = Color.grey;
								keyOnOff[4] = false;
							}
						}
					},
					126, {
						case
						{checker == 0} {
							case
							{mode == 0} {
								lowbuttons[0].background = Color.red;
								keyOnOff[5] = true;

								liveLoopGeneration.clearLastLoop;
							}
							{mode == 1} {
								lowbuttons[0].background = Color.red;
								keyOnOff[5] = true;
								beatGeneration.setKickIn(true);
								beatStart = (recordTime - currentBarNo);
								"beat Start".postln;
								beatStart.postln;
								beatGeneration.setBeatStart(beatStart);
								beatGeneration.inputFunc();
								intelligentResponse.stopKickResponse;
							}
							{mode == 2} {
								case
								{muteBeats == false} {
									lowbuttons[0].background = Color.red;
									muteBeats = true;
									beatGeneration.muteBeats;
								}
								{muteBeats == true} {
									lowbuttons[0].background = Color.grey;
									muteBeats = false;
									beatGeneration.unmuteBeats;
								};
							}
						}
						{keyOnOff[5] == true} {
							case
							{mode == 0} {
								lowbuttons[0].background = Color.grey;
								keyOnOff[5] = false;
							}
							{mode == 1} {
								lowbuttons[0].background = Color.grey;
								keyOnOff[5] = false;
								beatGeneration.stopRoutine();
								beatGeneration.findBeat();

								intelligentResponse.respond();

								intelligentResponse.kickResponse(beatGeneration.getLevelArray, beatGeneration.getDurArray, beatStart);

								case
								{muteBeats == true} {
									beatGeneration.muteBeats;
								}
								{muteResponse == true} {
									intelligentResponse.muteBeatResponses;
								}
								{muteAll == true} {
									intelligentResponse.muteBeatResponses;
									beatGeneration.muteBeats;
								}
							}
							{mode == 2} {
								lowbuttons[0].background = Color.grey;
								keyOnOff[5] = false;
							}
						}
					},
					123, {
						case
						{checker == 0} {

							case
							{mode == 0} {
								lowbuttons[1].background = Color.red;
								keyOnOff[6] = true;
								liveLoopGeneration.stopLoops;
							}
							{mode == 1} {
								lowbuttons[1].background = Color.red;
								keyOnOff[6] = true;
								beatGeneration.setSnareIn(true);
								beatStart = (recordTime - currentBarNo);
								"beat Start".postln;
								beatStart.postln;
								beatGeneration.setBeatStart(beatStart);
								beatGeneration.inputFunc();
								intelligentResponse.stopSnareResponse;
							}
							{mode == 2} {
								case
								{muteLoops == false} {
									lowbuttons[1].background = Color.red;
									muteLoops = true;
									liveLoopGeneration.muteLoops;
								}
								{muteLoops == true} {
									lowbuttons[1].background = Color.grey;
									muteLoops = false;
									liveLoopGeneration.unmuteLoops;
								};
							}
						}
						{keyOnOff[6] == true} {
							case
							{mode == 0} {
								lowbuttons[1].background = Color.grey;
								keyOnOff[6] = false;
							}
							{mode == 1} {
								lowbuttons[1].background = Color.grey;
								keyOnOff[6] = false;
								beatGeneration.stopRoutine();
								beatGeneration.findBeat();
								intelligentResponse.respond();
								intelligentResponse.snareResponse(beatGeneration.getLevelArray, beatGeneration.getDurArray, beatStart);
								// Check mutes
								case
								{muteBeats == true} {
									beatGeneration.muteBeats;
								}
								{muteResponse == true} {
									intelligentResponse.muteBeatResponses;
								}
								{muteAll == true} {
									intelligentResponse.muteBeatResponses;
									beatGeneration.muteBeats;
								}
							}
							{mode == 2} {
								lowbuttons[1].background = Color.grey;
								keyOnOff[6] = false;
							}
						}
					},
					124, {
						case
						{checker == 0} {

							case
							{mode == 0} {
								lowbuttons[2].background = Color.red;
								keyOnOff[7] = true;
							}
							{mode == 1} {
								lowbuttons[2].background = Color.red;
								keyOnOff[7] = true;
								beatGeneration.setHatIn(true);
								beatStart = (recordTime - currentBarNo);
								"beat Start".postln;
								beatStart.postln;
								beatGeneration.setBeatStart(beatStart);
								beatGeneration.inputFunc();
								intelligentResponse.stopHatResponse;
							}
							{mode == 2} {
								case
								{muteResponse == false} {
									lowbuttons[2].background = Color.red;
									muteResponse = true;
									intelligentResponse.muteResponses;
								}
								{muteResponse == true} {
									lowbuttons[2].background = Color.grey;
									muteResponse = false;
									intelligentResponse.unmuteResponses;
								};
							}
						}
						{keyOnOff[7] == true} {
							case
							{mode == 0} {
								lowbuttons[2].background = Color.grey;
								keyOnOff[7] = false;
							}
							{mode == 1} {
								lowbuttons[2].background = Color.grey;
								keyOnOff[7] = false;
								beatGeneration.stopRoutine();
								beatGeneration.findBeat();
								intelligentResponse.respond();
								intelligentResponse.hatResponse(beatGeneration.getLevelArray, beatGeneration.getDurArray, beatStart);
								case
								{muteBeats == true} {
									beatGeneration.muteBeats;
								}
								{muteResponse == true} {
									intelligentResponse.muteBeatResponses;
								}
								{muteAll == true} {
									intelligentResponse.muteBeatResponses;
									beatGeneration.muteBeats;
								}
							}
							{mode == 2} {

								lowbuttons[2].background = Color.grey;
								keyOnOff[7] = false;
							}
						}
					}
				);
			};

			// Key Button Actions
			w.view.keyDownAction = {
				arg view, char, modifiers, unicode, keycode;
				// has to be -0.25 because compensating for audio execution delay.
				recordTime = (tempo.beats.round(0.25)-0.35).round(0.25);
				key = keycode;
				keyFunc.value;
			};
			w.view.keyUpAction = {
				arg view, char, modifiers, unicode, keycode;
				key = keycode;
			};

			// Start in Generative Mode
			loopingMode.value;

			// Show GUI
			w.front;

			// On close free all and quit server
			w.onClose_({s.freeAll; s.stop; s.quit;});

		};

		// Assign server, flush it out and execute functions in order.
		s = Server.local;
		s.stop;
		s.quit;
		s.freeAll;
		s.newBufferAllocators;
		s.waitForBoot(loadSynths);
		s.waitForBoot(assignSynths);
		s.waitForBoot(setGUI);
	}
}