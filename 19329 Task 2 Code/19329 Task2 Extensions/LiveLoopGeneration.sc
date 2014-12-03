/*
Loop Generation Class:
Takes live input and creates loops.
It calculates the nearest bar, and contracts or expands the data and conforms to whole bar loops.
If running over input is cut to make the whole bar.
If running under silence is added make the whole bar.
Therefore it means everything always conforms to whole bar loops, which is ideal for loooping.
Loops playback at the start of the next whole bar.

Part of the Interactive/Responsive Looping and Beat Generation Interface.
(C) 2014-2015, Candidate Number 19329
Advanced Computer Music, University of Sussex, Spring 2014.
*/

LiveLoopGeneration {

	var s, p;

	// Methods:
	var responder;
	var inputFunc;
	var input;
	var stopRoutine;
	var findKey;

	// Key Detection variables
	var pchistogram;
	var pitches;
	var keyscores;
	var tmp, best;
	var keyFound;

	// Key detection variables
	var major;
	var minor;
	var kkminor;
	var kkmajor;

	// Keyboard input
	var keyFunc;
	var key = 0;
	var flex = 0;
	var buttons12;
	var buttons34;
	var buttonFlex;
	var pitchTracking = 0;

	// Feature Arrays
	var <>pitchArray;
	var ampArray;
	var tempo;
	var tempoMean;
	var liveBuffer;
	var liveRecording;
	var livePlayback;


	// Full amplitude array for audio playback and also bar conformity.
	var fullAmp;


	// Beat Arrays (for conformity, which all should abide by)
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

	// Root Note Variables
	var rootN;
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

	// Scale and key variables for class extraction
	var <>scale;
	var <>keyFinal;

	// Multiples of four (for rhythm conformity)
	var multiplesFour;

	// Tempo clocks
	var tempoBeat;

	var liveIn;

	var loopArray;

	var majminLetter;
	var keyLetter1, keyLetter2;

	var liveArray;

	var <>beatStart = 0;
	var currentBarNo = 0;

	var synthCounter = 0;

	var currentBar = 0;

	*new {
		^super.newCopyArgs.detect();
	}

	detect {

		loopArray = List();

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

		// Fill with multiples of four
		multiplesFour = Array.fill(400, {arg i;
			i*4;
		});

		// Remove 0 from the multiple array
		multiplesFour.removeAt(0);
		s = Server.local;

		// set the tempo clock
		tempoBeat = TempoClock.new(2, 0, Main.elapsedTime.ceil);


		// Invoke the responder method
		this.responder();
	}

	responder {OSCresponder(s.addr,'looptrack', { arg time, responder, msg;

		// 3=freq 4=hasFreq 5=gate 6=pitch 7=amp/vel 8=tempo 9=ons Onset

		// for live input always always add to the livedurArray
		liveDurArray.add(0.046439909297052);

		// gate information to be used for 'beats' in generative mode (but rounded to 1.0) -- take gate info!
		beatGate.add(msg[5].round);

		// Changing the code above to be smaller than 0 do nothing, else above 0 so onset detected do something
		if(msg[9] > 0, {testGate.add(1); durArray.add(0.046439909297052)}, {testGate.add(0); durArray.add(0.046439909297052)});

		// testing the amplitude values for buffer placyback
		fullAmp.add(msg[7]);


		// tester for the rate of posting (for tempo and beat matching soon)

		// Execute following code, only if the gate threshold has been exceeded
		if(msg[5] == 1, {
			// Adding the midi converted frequencies into a pitch array
			pitchArray.add(msg[6]);

			// Add the amplitude values into an array
			ampArray.add(msg[7]);
			// Tempo array
			tempo.add(msg[8]);
		});

		}).add;

	}

	// Input Method:
	inputFunc
	{
		liveIn = true;

		pitchArray = List();
		ampArray = List();
		tempo = List();
		beatGate = List();
		testGate = List();
		durArray = List();
		fullAmp = List();
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

			SendReply.kr(Impulse.kr(44100/1024),'looptrack',[freq,hasFreq, gate, pitch, amp, tempo, ons]);
		}.play;
	}



	// Stop the live input tracking
	stopRoutine {
		input.free;
		liveRecording.free;
		liveIn = false;
	}

	findKey {
		// Get mean of tempo here to avoid error in repsonder
		tempoMean = tempo.mean;

		// Don't want to use this here! -- Also want to store in array first, conform then buffer and playback!
		// livePlayback = {Pan2.ar(PlayBuf.ar(1, liveBuffer.bufnum, loop:1) )}.play;
		liveBuffer.loadToFloatArray(action: { arg array; liveArray = array;});

		/*	 x = {Pan2.ar(PlayBuf.ar(1, liveBuffer.bufnum, loop:1) )}.play;*/






		liveDurCounter = 0;
		liveDurArray.do{arg i;
			liveDurCounter = liveDurCounter + liveDurArray[i];
		};


		liveNearestBar = multiplesFour.minItem{|x| (x-liveDurCounter).abs};

		case
		// Bigger
		{liveDurCounter > liveNearestBar} {
			while({liveDurCounter > liveNearestBar}, {
				liveArray.removeAt(liveArray.size-1);
				liveDurArray.removeAt(liveDurArray.size-1);
				liveDurCounter = liveDurCounter - 0.046439909297052;
			});

			// Show us the updated amount of bars. --- actually not amount of bars since 4 = 1bar.
			liveDurCounter = 0;
			liveDurArray.do{arg i;
				liveDurCounter = liveDurCounter + liveDurArray[i];
			};
		}
		{liveDurCounter < liveNearestBar} {
			while({liveDurCounter < liveNearestBar}, {
				liveArray.add(0);
				liveDurArray.add(0.046439909297052);
				liveDurCounter = liveDurCounter + 0.046439909297052;
			});

			// Show us the updated amount of bars. --- actually not amount of bars since 4 = 1bar.
			liveDurCounter = 0;
			liveDurArray.do{arg i;
				liveDurCounter = liveDurCounter + liveDurArray[i];
			};
		};

		loopArray.add( Pbind(
			\instrument, \oneLoop,
			\bufnum, liveBuffer,
			\amp, 0.3,
			\start, 0,
			\time, liveDurCounter.round(1.0),
			\dur, liveDurCounter.round(1.0)
		).play(clock: tempoBeat, quant: 4));


		// STAGE 1: SET IT UP
		pchistogram =Array.fill(12, {0.01}); //set to equal non zero values because of normalizeSum later

		pitches = pitchArray;

		// Dobt think I need this now I hve durArray
		durGate = Array.fill(testGate.size, {arg i; 0.25 });

		pitches.do{arg pitch;
			var p, pc;
			p = pitch;
			pc = pitch%12;

			// Store an array for our histogram of pitches!
			pchistogram[pc] = pchistogram[pc]+1;
		};

		// normalise the histogram
		pchistogram.normalizeSum;

		// STAGE 2: FIND KEY
		keyscores=Array.fill(24,{0});

		24.do{arg i;
			var testkey, score, results;

			testkey= (i.div(2)+(if(i.odd,{major},{minor})))%12;

			//instead, adjust for Krumhansl Kessler profiles
			results= if(i.odd,{kkmajor},{kkminor});

			results= results.rotate(i.div(2));

			score=0;

			pchistogram.do({arg num,i;

				score=score+(num*(results[i]));
			});

			keyscores[i]=score;

			// Testing all 24 keys for their probability of which the melody fits best
		};

		//highest score wins!
		tmp=0;
		best=(1000.neg);
		keyscores.do{arg val,j; if(val>best,{best=val; tmp=j});};

		// Give me the correct key!
		keyFound = [\key, (["C","Db","D","Eb","E","F","F#","G","Ab","A","Bb","B"].at(tmp.div(2)))+(if(tmp.odd,"major","minor")),\keyarray,(tmp.div(2)+(if(tmp.odd,{major},{minor})))%12  ];


		// Possibly replace these variables (--- for retreiving and storing key for music generation)
		keyLetter1 = keyFound[1].at(0).asString;
		keyLetter2 = keyFound[1].at(1).asString;

		// Major or minor
		majminLetter = keyFound[1].at(3).asString;

		keyFinal = (keyLetter1+keyLetter2);


		switch(keyFinal,
			"C  ", {
				rootN = cRoots.choose;},
			"D b", {
				rootN = cSharpRoots.choose;},
			"D  ", {
				rootN = dRoots.choose;},
			"E b", {
				rootN = dSharpRoots.choose;},
			"E  ", {
				rootN = eRoots.choose;},
			"F  ", {
				rootN = fRoots.choose;},
			"G b", {
				rootN = fSharpRoots.choose},
			"G  ", {
				rootN = gRoots.choose;},
			"A b", {
				rootN = gSharpRoots.choose;},
			"A  ", {
				rootN = aRoots.choose;},
			"B b", {
				rootN = aSharpRoots.choose;},
			"B  ", {
				rootN = bRoots.choose;},
		);


		case
		{majminLetter == "a"} {scale = "major"}
		{majminLetter == "i"} {scale = "minor"};



		// CONFORMITY -- happenng after key detection and before they are wiped to nil
		durCounter = 0;
		durArray.do{arg i;
			durCounter = durCounter + durArray[i];
		};

		nearestBar = multiplesFour.minItem{|x| (x-durCounter).abs};

		case
		// Bigger
		{durCounter > nearestBar} {
			while({durCounter > nearestBar}, {
				testGate.removeAt(testGate.size-1);
				durArray.removeAt(durArray.size-1);
				durCounter = durCounter - 0.046439909297052;
			});

			// Show us the updated amount of bars. --- actually not amount of bars since 4 = 1bar.
			durCounter = 0;
			durArray.do{arg i;
				durCounter = durCounter + durArray[i];
			};
		}
		{durCounter < nearestBar} {
			while({durCounter < nearestBar}, {
				testGate.add(0);
				durArray.add(0.046439909297052);
				durCounter = durCounter + 0.046439909297052;
			});

			// Show us the updated amount of bars. --- actually not amount of bars since 4 = 1bar.
			durCounter = 0;
			durArray.do{arg i;
				durCounter = durCounter + durArray[i];
			};
		};

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

		quantisedOnsetArray.removeAt(quantisedOnsetArray.size-1);






		quantisedOnsetArray.size.do{arg i;
			quantisedDurArray.add(0.25);
		};




		quantisedDurArray = quantisedDurArray.as(Array);
		quantisedOnsetArray = quantisedOnsetArray.as(Array);



		ampArray = nil;
		// Wipe tempo Array and Tempo Mean
		tempo = nil;
		tempoMean = nil;
		// Wipe beat gate array
		beatGate = nil;
		// Wipe the tester beat gater for onset detection and 'level' in the patterns
		testGate = nil;
		durGate = nil;
		durArray = nil;

		fullAmp = nil;

		liveDurArray = nil;

		synthCounter = synthCounter + 1;
	}

	setBeatStart {arg beat;
		this.beatStart = beat;
	}

	stopLoops {
		loopArray.do{arg i;
			i.stop;
		};
		// And therefore allow the key and scale to be reset
		keyFinal = nil;
		scale = nil;
	}

	muteLoops {
		loopArray.do{arg i;
			i.mute;
		};
	}

	unmuteLoops {
		loopArray.do{arg i;
			i.unmute;
		};
	}

	clearLastLoop {
		loopArray[synthCounter-1].stop;
	}

	getKey {
		^keyFinal.value;
	}

	getScale {
		^scale.value;
	}

	getPitchArray {
		^pitchArray;
	}
}
