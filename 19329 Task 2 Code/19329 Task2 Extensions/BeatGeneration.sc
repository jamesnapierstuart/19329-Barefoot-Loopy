/*
Beat Generation Class:
Creates beats according to live input information.
Using onset detection, this class quantises onset signals into 1/16th notes.
Next it contracts or expands to conform the pattern to bar lengths.
Therefore ensuring each pattern's length can be measured in bars.
Beats play back at the next whole bar, plus the beat you 'punched in' on.

Part of the Interactive/Responsive Looping and Beat Generation Interface.
(C) 2014-2015, Candidate Number 19329
Advanced Computer Music, University of Sussex, Spring 2014.
*/

BeatGeneration {

	// Methods:
	var responder;
	var inputFunc;
	var input;
	var stopRoutine;
	var findBeat;
	var s, d;

	// Keyboard input
	var keyFunc;
	var key = 0;
	var flex = 0;
	var buttons12;
	var buttons34;
	var buttonFlex;
	var pitchTracking = 0;

	// Feature Arrays
	var pitchArray;
	var ampArray;
	var tempo;
	var tempoMean;
	var liveBuffer;
	var liveRecording;
	var livePlayback;

	// adding to class
	var <>tempoBeat;
	var synth;

	// Beat Arrays (for conformity)
	var beatGate;
	var testGate;
	var durGate;
	var durArray;
	var durCounter;
	var nearestBar;

	// variables for new quantised beat onset detector
	var quantisedOnsetArray;
	var quantisedDurArray;

	// Variables for live input conformity
	var liveDurCounter;
	var liveNearestBar;
	var liveDurArray;

	// Multiples of four (for rhythm conformity)
	var multiplesFour;

	// beat Patterns
	var kickPattern;
	var snarePattern;
	var hatPattern;
	var tomPattern;

	// Kick Snare or Hat
	var <>kickIn = false;
	var <>snareIn = false;
	var <>hatIn = false;
	var <>tomIn = false;
	var liveIn = false;

	// beat starting
	var <>beatStart = 0;
	var currentBarNo = 0;
	var tempoPattern;
	var synths;

	*new {
		^super.newCopyArgs.detect();
	}

	// Detect Method:
	detect {

		// Multiples of four, for conformity
		multiplesFour = Array.fill(400, {arg i;
			i*4;
		});

		// Remove 0 from the multiple array
		multiplesFour.removeAt(0);

		// Set Server
		s = Server.local;

		// Set tempo
		tempoBeat = TempoClock.new(2, 0, Main.elapsedTime.ceil);


		// Execute the responder from the start.
		this.responder();
	}

	// OSC repsonder method "CHANGE THE MESSAGE FROM OSC"
	responder {OSCresponder(s.addr,'notestore', { arg time, responder, msg;

		// 3=freq 4=hasFreq 5=gate 6=pitch 7=amp/vel 8=tempo 9=ons Onset

		// for live input always always add to the livedurArray
		liveDurArray.add(0.046439909297052);

		// gate information to be used for 'beats' in generative mode (but rounded to 1.0) -- take gate info!
		beatGate.add(msg[5].round);

		// Changing the code above to be smaller than 0 do nothing, else above 0 so onset detected do something
		// This second threshhold number needs to be altered depending on instrument and background noise
		if(msg[9] > 0, {testGate.add(1); durArray.add(0.046439909297052)}, {testGate.add(0); durArray.add(0.046439909297052)});

		}).add;
	}

	// Input Method:
	inputFunc
	{
		case
		{kickIn == true} {
			kickPattern.stop;
		}
		{snareIn == true} {
			snarePattern.stop;
		}
		{hatIn == true} {
			hatPattern.stop;
		}
		{tomIn == true} {
			tomPattern.stop;
		};

		liveIn = true;

		// Set MIR lists
		tempo = List();
		beatGate = List();
		testGate = List();
		durArray = List();
		liveDurArray = List();
		quantisedOnsetArray = List();
		quantisedDurArray = List();

		// This 20 before the 44100 means 20 seconds worth of recording before it records over itself.
		liveBuffer = Buffer.alloc(s, 20*44100, 1);

		case
		{livePlayback != nil} {livePlayback.free;};
		input =
		{
			var soundin, freq, hasFreq = 0.0;
			var amp, threshhold, gate;
			var pitch;
			var trackb,trackh,trackq,tempo;
			var env;
			var trig;
			var ons;

			soundin = SoundIn.ar(0);
			#freq, hasFreq = Tartini.kr(soundin);
			amp = Amplitude.kr(soundin);
			threshhold = 0.02;
			gate = Lag.kr(amp > threshhold, 0.001);
			pitch = freq.cpsmidi.round(1);

			#trackb,trackh,trackq,tempo=BeatTrack.kr(FFT(LocalBuf(1024), soundin));
			env = EnvGen.ar(Env.adsr, gate);
			liveRecording = RecordBuf.ar(soundin*env, liveBuffer.bufnum);

			// Real Onset detection for beat generation
			ons = PinkNoise.ar(Decay.kr(Coyote.kr(SoundIn.ar, thresh:0.2),0.02));

			SendReply.kr(Impulse.kr(44100/1024),'notestore',[freq,hasFreq, gate, pitch, amp, tempo, ons]);
		}.play;
	}

	// Stop the live input tracking
	stopRoutine {
		input.free;
		liveRecording.free;
		liveIn = false;
	}

	// Beat Detection
	findBeat {

		// Counter for note durations
		durCounter = 0;
		durArray.do{arg i;
			durCounter = durCounter + durArray[i];
		};

		// Find nearest bar
		nearestBar = multiplesFour.minItem{|x| (x-durCounter).abs};

		// Increase or Decrease data, to the nearest bar.
		case
		// Bigger
		{durCounter > nearestBar} {
			while({durCounter > nearestBar}, {
				testGate.removeAt(testGate.size-1);
				durArray.removeAt(durArray.size-1);
				durCounter = durCounter - 0.046439909297052;
			});
		}
		// Smaller
		{durCounter < nearestBar} {
			while({durCounter < nearestBar}, {
				testGate.add(0);
				durArray.add(0.046439909297052);
				durCounter = durCounter + 0.046439909297052;
			});
		};

		// Quantise the data, by assigning 1/16th note durations (0.25)
		testGate.size.do{arg i;
			if(i == 0, {}, {
				// else if i%5 == 0
				if(i+1%5 == 0, {
					// add up previous 5 numbers must be [i]
					if((testGate[i] + testGate[i-1] + testGate[i-2] + testGate[i-3]+ testGate[i-4]) != 0, {
						// add 1 to the new array (list)
						quantisedOnsetArray.add(1);
						},
						{
							// else add 0
							quantisedOnsetArray.add(0);
					});
				});
			});
		};

		// Remove the last array value to make 16 values (0.25*16 = 4) A full bar.
		quantisedOnsetArray.removeAt(quantisedOnsetArray.size-1);
		quantisedOnsetArray.size.do{arg i;
			quantisedDurArray.add(0.25);
		};

		// Turn into arrays for PBind
		quantisedDurArray = quantisedDurArray.as(Array);
		quantisedOnsetArray = quantisedOnsetArray.as(Array);

		// Choose the drum pattern to playback
		case
		{kickIn == true} {

			kickPattern = Pbind(
				\instrument, \kickJ,
				\attack, 0.001,
				\level, Pseq([Pseq(quantisedOnsetArray, inf)]),
				\dur, Pseq([Pseq(quantisedDurArray, inf)]),
			).play(clock: tempoBeat, quant: 4 + beatStart);
		}
		{snareIn == true} {
			snarePattern = Pbind(
				\instrument, \snareJ,
				\attack, 0.001,
				\level, Pseq([Pseq(quantisedOnsetArray, inf)]),
				\dur, Pseq([Pseq(quantisedDurArray, inf)]),
			).play(clock: tempoBeat, quant: 4 + beatStart);
		}
		{hatIn == true} {
			hatPattern = Pbind(
				\instrument, \hat1J,
				\attack, 0.001,
				\level, Pseq([Pseq(quantisedOnsetArray, inf)]),
				\dur, Pseq([Pseq(quantisedDurArray, inf)]),
			).play(clock: tempoBeat, quant: 4 + beatStart);
		}
		{tomIn == true} {
			tomPattern = Pbind(
				\instrument, \tomJ,
				\attack, 0.001,
				\level, Pseq([Pseq(quantisedOnsetArray, inf)]),
				\dur, Pseq([Pseq(quantisedDurArray, inf)]),
			).play(clock: tempoBeat, quant: 4 + beatStart);
		};

		// Set the Ins to false, for the next input.
		kickIn = false;
		snareIn = false;
		hatIn = false;
		tomIn = false;
	}

	setTempo {arg tempo;
		this.tempoBeat = tempo;
	}

	setKickIn {arg kickIn;
		this.kickIn = kickIn;
	}

	setSnareIn {arg snareIn;
		this.snareIn = snareIn;
	}

	setHatIn {arg hatIn;
		this.hatIn = hatIn;
	}

	setTomIn {arg tomIn;
		this.tomIn = tomIn;
	}

	setBeatStart {arg beat;
		this.beatStart = beat;
	}

	muteBeats {
		if(kickPattern != nil, {kickPattern.mute});
		if(snarePattern != nil, {snarePattern.mute});
		if(hatPattern != nil, {hatPattern.mute});
		if(tomPattern != nil, {tomPattern.mute});
	}

	unmuteBeats {
		if(kickPattern != nil, {kickPattern.unmute});
		if(snarePattern != nil, {snarePattern.unmute});
		if(hatPattern != nil, {hatPattern.unmute});
		if(tomPattern != nil, {tomPattern.unmute});
	}

	stopBeats {
		kickPattern.stop;
		snarePattern.stop;
		hatPattern.stop;
		tomPattern.stop;
	}

	getLevelArray {
		^quantisedOnsetArray;
	}

	getDurArray {
		^quantisedDurArray;
	}
}