/*
Intelligent Response Class:
Creates intelligent responses in the form of synthesized patterns.
Determined by information such as key, pitch, onsets retrieved from live input.

Part of the Interactive/Responsive Looping and Beat Generation Interface.
(C) 2014-2015, Candidate Number 19329
Advanced Computer Music, University of Sussex, Spring 2014.
*/

IntelligentResponse {

	// Scale and key variables for class extraction (Cmaj to begin with)
	var <>key;
	var <>scale = "major";
	var <>pitches;

	// Key detection variables
	var major;
	var minor;
	var kkminor;
	var kkmajor;

	// Root Note Variables
	var rootN = 60;
	var cRoots;
	var cSharpRoots;
	var dRoots;
	var dSharpRoots;
	var eRoots;
	var fRoots;
	var fSharpRoots;
	var gRoots;
	var gSharpRoots;
	var aRoots;
	var aSharpRoots;
	var bRoots;

	// Beat roots ;)
	var bassRoot = 36;
	var snareRoot = 60;
	var tomRoot = 72;
	var hatRoot = 84;
	var finalRoot = 0;

	// Tempo Clock
	var tempoBeat;

	// Scale Synth
	var scaleSynth;

	// Beat and loop responders
	var snareResponseList;
	var snareCounter = 0;
	var kickResponseList;
	var kickCounter = 0;
	var tomResponseList;
	var hatResponseList;
	var loopResponseList;
	var degrees;

	*new {
		^super.newCopyArgs.assign();
	}

	assign {
		snareResponseList = List();
		kickResponseList = List();
		tomResponseList = List();
		hatResponseList = List();
		loopResponseList = List();
		degrees = Array.fill(7);
	}

	setTempo {
		// set the tempo clock
		tempoBeat = TempoClock.new(2, 0, Main.elapsedTime.ceil);
	}

	respond {

		// Assign Variables:
		major= [0,2,4,5,7,9,11];
		minor= [0,2,3,5,7,8,11];
		kkminor= [ 0.14221523253202, 0.060211188496967, 0.079083352055718, 0.12087171422152, 0.05841383958661, 0.079308020669512, 0.057065827903842, 0.10671759155246, 0.089418108290272, 0.060435857110762, 0.075039317007414, 0.071219950572905 ];
		kkmajor = [ 0.15195022732711, 0.053362048336923, 0.083273510409189, 0.055754965302704, 0.10480976310122, 0.097870303900455, 0.060301507537688, 0.12419239052405, 0.057190715482173, 0.087580760947595, 0.054797798516391, 0.068916008614501 ];

		cRoots =      [24, 36, 48, 60, 72, 84];
		cSharpRoots = [25, 37, 49, 61, 73, 85];
		dRoots =      [26, 38, 50, 62, 74, 86];
		dSharpRoots = [27, 39, 51, 63, 75, 87];
		eRoots =      [28, 40, 52, 64, 76, 88];
		fRoots =      [29, 41, 53, 65, 77, 89];
		fSharpRoots = [30, 42, 54, 66, 78, 90];
		gRoots =      [31, 43, 55, 67, 79, 91];
		gSharpRoots = [32, 44, 56, 68, 80, 92];
		aRoots =      [33, 45, 57, 69, 81, 93];
		aSharpRoots = [34, 46, 58, 70, 82, 94];
		bRoots =      [35, 47, 59, 71, 83, 95];

		// catch it if it is nil on incoming -- what happens if it is set though?
		case{key != nil} {

			// Choose a root based upon the key
			switch(key,
				"C  ", {
					rootN = cRoots.choose;
					finalRoot = 0;
				},
				"D b", {
					rootN = cSharpRoots.choose;
					finalRoot = 1;
				},
				"D  ", {
					rootN = dRoots.choose;
					finalRoot = 2;
				},
				"E b", {
					rootN = dSharpRoots.choose;
					finalRoot = 3;
				},
				"E  ", {
					rootN = eRoots.choose;
					finalRoot = 4;
				},
				"F  ", {
					rootN = fRoots.choose;
					finalRoot = 5;
				},
				"G b", {
					rootN = fSharpRoots.choose;
					finalRoot = 6;
				},
				"G  ", {
					rootN = gRoots.choose;
					finalRoot = 7;
				},
				"A b", {
					rootN = gSharpRoots.choose;
					finalRoot = 8;
				},
				"A  ", {
					rootN = aRoots.choose;
					finalRoot = 9;
				},
				"B b", {
					rootN = aSharpRoots.choose;
					finalRoot = 10;
				},
				"B  ", {
					rootN = bRoots.choose;
					finalRoot = 11;
				},
			);


			case
			{scale != "major" || scale != "minor"} {scale = "major"};

			// Now use scale.
			case
			{scale == "major"} {
				scaleSynth = Scale.major;
			}
			{scale == "minor"} {
				scaleSynth = Scale.minor;
			};

			case
			{scale == "major"} {
				degrees =  [0,2,4,5,7,9,11];
			}
			{scale == "minor"} {
				degrees =  [0,2,3,5,7,8,11];
			}
		};

	}

	// Kick repsonse
	kickResponse {arg levelArray, durArray, beatStart;
		var midiArray = List();
		case
		{scale == "major"} {
			levelArray.do{arg i;
				case
				{i == 1} {midiArray.add(rrand(0,2,4,5,7,9,11))}
				{i == 0} {midiArray.add(\rest)}
			};
		}
		{scale == "minor"} {
			levelArray.do{arg i;
				case
				{i == 1} {midiArray.add(rrand(0,2,3,5,7,8,11))}
				{i == 0} {midiArray.add(\rest)}
			};
		};

		kickResponseList.add(Pbind(
			\instrument, \solo1J,
			\attack, 0.1,
			\release, 2,
			\oscType, 0,
			\lforate, 3,
			\root, finalRoot, \octave, 4, \degree, Pseq(midiArray, inf), \dur, 0.25,
			).play(clock: tempoBeat, quant: 4+beatStart);
		);
		kickCounter = kickCounter + 1;
	}

	// Snare repsonse
	snareResponse {arg levelArray, durArray, beatStart;
		var midiArray = List();
		case
		{scale == "major"} {
			levelArray.do{arg i;
				case
				{i == 1} {midiArray.add(rrand(0,2,4,5,7,9,11, \rest))}
				{i == 0} {midiArray.add(\rest)}
			};
		}
		{scale == "minor"} {
			levelArray.do{arg i;
				case
				{i == 1} {midiArray.add(rrand(0,2,3,5,7,8,11, \rest))}
				{i == 0} {midiArray.add(\rest)}
			};
		};
		snareResponseList.add(Pbind(
			\instrument, \solo1J,
			\amp, 0.1,
			\release, 1,
			\lforate, Prand([0.1, 5, 1, 3, 10], inf),
			\cutofffreq, Prand([500, 400, 1000, 900, 700], inf),
			\root, finalRoot, \octave, 4, \degree, Pseq(midiArray, inf), \dur, 0.25,
			).play(clock: tempoBeat, quant: 4+beatStart);
		);
		snareCounter = snareCounter + 1;
	}

	// Tom repsonse
	tomResponse {arg levelArray, durArray, beatStart;
		var midiArray = List();
		var scaley = Scale.major;
		case
		{scale == "major"} {
			scaley = Scale.major;
			levelArray.do{arg i;
				case
				{i == 1} {midiArray.add(rrand(0,2,4,5,7,9,11, \rest))}
				{i == 0} {midiArray.add(\rest)}
			};
		}
		{scale == "minor"} {
			scaley = Scale.minor;
			levelArray.do{arg i;
				case
				{i == 1} {midiArray.add(rrand(0,2,3,5,7,8,11, \rest))}
				{i == 0} {midiArray.add(\rest)}
			};
		};

		tomResponseList.add(Pbind(
			\instrument, \chrd1J,
			\amp, 0.1,
			\attack, 0.001,
			\release, 0.5,
			\filtType, 2,
			\oscType, 2,
			\envType, 1,
			\root, finalRoot, \octave, Prand([5, 6], inf),
			\degree, Pseq(midiArray, inf),
			\dur, 0.25,
			).play(clock: tempoBeat, quant: 4+beatStart);
		);
	}

	// Hihat repsonse
	hatResponse {arg levelArray, durArray, beatStart;
		var midiArray = List();
		var scaley = Scale.major;

		case
		{scale == "major"} {
			scaley = Scale.major;
			levelArray.do{arg i;
				case
				{i == 0} {midiArray.add(rrand(0,2,4,5,7,9,11, \rest))}
				{i == 1} {midiArray.add(\rest)}
			};
		}
		{scale == "minor"} {
			scaley = Scale.minor;
			levelArray.do{arg i;
				case
				{i == 0} {midiArray.add(rrand(0,2,3,5,7,8,11, \rest))}
				{i == 1} {midiArray.add(\rest)}
			};
		};

		hatResponseList.add(Pbind(
			\instrument, \solo1J,
			\amp, 0.1,
			\attack, 0.001,
			\release, 0.5,
			\filtType, 2,
			\oscType, 2,
			\envType, 1,
			\root, finalRoot, \octave, Prand([5, 6], inf),
			\degree, Pseq(midiArray, inf),
			\dur, 0.25,
			).play(clock: tempoBeat, quant: 4+beatStart);
		);
	}

	// Loop repsonse
	loopResponse {arg pitchArray;
		var scaley = Scale.major;
		case
		{scale == "major"} {
			scaley = Scale.major;
		}
		{scale == "minor"} {
			scaley = Scale.minor;
		};

		case
		{pitchArray == nil} {pitchArray = Array.fill(7, {arg i; rrand(50, 80)})};

		loopResponseList.add(Pbind(
			\instrument, \solo1J,
			\amp, 0.1,
			\attack, 0.1,
			\release, 3,
			\sustain, 1,
			\midinote, Prand(pitchArray, inf),
			\dur, 4,
			).play(clock: tempoBeat, quant: 4);
		);
	}


	setKey {arg key;
		this.key = key;
	}

	setScale {arg scale;
		this.scale = scale;
	}

	setPitches {arg pitches;
		this.pitches = pitches;
	}

	muteResponses {
		kickResponseList.do{arg i;
			i.mute;
		};
		snareResponseList.do{arg i;
			i.mute;
		};
		tomResponseList.do{arg i;
			i.mute;
		};
		hatResponseList.do{arg i;
			i.mute;
		};
		loopResponseList.do{arg i;
			i.mute;
		};
	}

	muteBeatResponses {
		kickResponseList.do{arg i;
			i.mute;
		};
		snareResponseList.do{arg i;
			i.mute;
		};
		tomResponseList.do{arg i;
			i.mute;
		};
		hatResponseList.do{arg i;
			i.mute;
		};
	}

	unmuteBeatResponses {
		kickResponseList.do{arg i;
			i.mute;
		};
		snareResponseList.do{arg i;
			i.mute;
		};
		tomResponseList.do{arg i;
			i.mute;
		};
		hatResponseList.do{arg i;
			i.mute;
		};
	}

	unmuteResponses {
		kickResponseList.do{arg i;
			i.unmute;
		};
		snareResponseList.do{arg i;
			i.unmute;
		};
		tomResponseList.do{arg i;
			i.unmute;
		};
		hatResponseList.do{arg i;
			i.unmute;
		};
		loopResponseList.do{arg i;
			i.unmute;
		};
	}

	// Stop all responses (loops and beats)
	stopResponses {
		kickResponseList.do{arg i;
			i.stop;
		};
		snareResponseList.do{arg i;
			i.stop;
		};
		tomResponseList.do{arg i;
			i.stop;
		};
		hatResponseList.do{arg i;
			i.stop;
		};
		loopResponseList.do{arg i;
			i.stop;
		};
	}

	// Stop all loop responses
	stopLoopResponses {
		loopResponseList.do{arg i;
			i.stop;
		};
	}

	// stop all beat responses
	stopBeatResponses {
		kickResponseList.do{arg i;
			i.stop;
		};
		snareResponseList.do{arg i;
			i.stop;
		};
		tomResponseList.do{arg i;
			i.stop;
		};
		hatResponseList.do{arg i;
			i.stop;
		};
	}

	// Snare Stop
	stopSnareResponse {
		snareResponseList.do{arg i;
			i.stop;
		};
	}

	// Kick Stop
	stopKickResponse {
		kickResponseList.do{arg i;
			i.stop;
		};
	}

	// Tom Stop
	stopTomResponse {
		tomResponseList.do{arg i;
			i.stop;
		};
	}

	// Hat Stop
	stopHatResponse {
		hatResponseList.do{arg i;
			i.stop;
		};
	}

	// Mute Loop responses
	muteLoopResponses {
		loopResponseList.do{arg i;
			i.mute;
		};
	}

	// Unmute Loop responses
	unmuteLoopResponses {
		loopResponseList.do{arg i;
			i.unmute;
		};
	}
}