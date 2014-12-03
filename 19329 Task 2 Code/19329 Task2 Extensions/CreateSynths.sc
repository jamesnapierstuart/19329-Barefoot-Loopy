/*
Create Synth Class:
Stores specific SynthDefs to the server.
Synth instances then called by beat/loop/repsonse classes.

Part of the Interactive/Responsive Looping and Beat Generation Interface.
(C) 2014-2015, Candidate Number 19329
Advanced Computer Music, University of Sussex, Spring 2014.
*/

CreateSynths {

	*new {
		^super.newCopyArgs();
	}

	create {

		SynthDef(\tempoClick, {arg out = 0, amp = 0.05, freq = 500, t = 2, gate = 1;
			var sound, env;
			sound = Ringz.ar(Impulse.ar(t), [freq], 1/2);
			env = EnvGen.ar(Env.adsr, gate, doneAction: 2);
			Out.ar(out, Pan2.ar(sound*env)*amp);
		}).store;

		SynthDef(\dryInput, {arg out = 0, amp = 0.5;
			var sound, vel, threshhold, gate, env;
			sound = SoundIn.ar(0);
			vel = Amplitude.kr(sound);
			threshhold = 0.1;
			gate = Lag.kr(vel > threshhold, 0.1);
			env = EnvGen.ar(Env.adsr, gate);
			Out.ar(out, Pan2.ar(sound*env)*amp);
		}).store;

		SynthDef('blip',
			{
				arg freq = 440, amp = 0.1, dur = 1, gate = 1;
				var ampEnv, osc;
				ampEnv = EnvGen.ar(Env.adsr(0.001, 0.1, 0.1, 0.1),gate, doneAction: 2);
				osc = SinOsc.ar(freq, 0, ampEnv);
				Out.ar(0, osc!2*amp);
		}).store;

		// Synth chord 1:
		SynthDef(\chrd1J, {arg freq = 440, harmony1 = 440, harmony2 = 440, harmony3 = 440, harmony4 = 440, oscType = 2, filtType= 1, envType = 1, lfoType = 0, lforate = 1, cutofffreq = 200, startfreq = 60, endfreq = 10000, filtdur = 1, rq = 0.3, attack = 0.6, decay = 3, sustain = 0, release = 3, envlevel = 1, curve = -6, start = 1.0, end = 0, dura = 1.0,  gate = 1, amp = 0.05;
			var oscArray, envArray, filtArray, lfoArray, sound, env, root, lfo, endi, outi;
			lfoArray = [
				SinOsc.ar(lforate),
				Saw.ar(lforate),
				Pulse.ar(lforate)];
			lfo = (Select.ar(lfoType, lfoArray));
			oscArray = [
				Mix(SinOsc.ar([freq, (freq+harmony1), (freq+harmony2), (freq+harmony3), (freq+harmony4)], 0, 0.2)),
				Mix(Saw.ar([([0.995, 1, 1.02, 1.5] * freq), ([0.995, 1, 1.02, 1.5] * (freq+harmony1)), ([0.995, 1, 1.02, 1.5] *(freq+harmony2)), ([0.995, 1, 1.02, 1.5] * (freq+harmony3)), ([0.995, 1, 1.02, 1.5] * (freq+harmony4)),], 0.2)),
				Mix(Pulse.ar([([0.995, 1, 1.02, 1.5] * freq), ([0.995, 1, 1.02, 1.5] * (freq+harmony1)), ([0.995, 1, 1.02, 1.5] *(freq+harmony2)), ([0.995, 1, 1.02, 1.5] * (freq+harmony3)), ([0.995, 1, 1.02, 1.5] * (freq+harmony4)),], 0.2, 0.2)),
				Mix(LFTri.ar([([0.995, 1, 1.02, 1.5] * freq), ([0.995, 1, 1.02, 1.5] * (freq+harmony1)), ([0.995, 1, 1.02, 1.5] *(freq+harmony2)), ([0.995, 1, 1.02, 1.5] * (freq+harmony3)), ([0.995, 1, 1.02, 1.5] * (freq+harmony4)),], 0.2, 0.2))];
			filtArray = [
				Select.ar(oscType, oscArray), // Off
				RLPF.ar(Select.ar(oscType, oscArray), Line.kr(startfreq, endfreq, filtdur), rq),
				RHPF.ar(Select.ar(oscType, oscArray), Line.kr(startfreq, endfreq, filtdur), rq),
				MoogFF.ar(Select.ar(oscType, oscArray),  Line.kr(startfreq, endfreq, filtdur), rq)];
			envArray = [EnvGen.kr(Env.adsr(attack, decay, sustain, release), gate, doneAction:2),
				EnvGen.kr(Env.perc(attack, release, envlevel, curve), gate, doneAction:2),
				Line.kr(start, end, dura)];
			env = Select.kr(envType, envArray);
			sound = (Select.ar(filtType, filtArray)) * lfo;
			Out.ar(0, Pan2.ar(sound*env*amp));
		}).store;

		SynthDef(\kickJ,{
			arg freq = 240, amp=0.5, attack = 0.001, level = 1, gate = 1;
			var sound, env, sub, subenv, subout, click, clickenv, clickout;
			sub = SinOsc.ar(freq/4);
			subenv = EnvGen.kr(Env.adsr(attack, 0.2, 0, 1, 1), gate, level, doneAction:0.2);
			subout = (sub * subenv);
			env = EnvGen.kr(Env.adsr(attack, 0.2,  0, 1, 1), gate, level, doneAction:2);
			sound = SinOsc.ar(Line.kr(freq,60,0.05))*env;
			Out.ar(0, Pan2.ar(sound + subout)*amp)
		}).store;

		SynthDef(\snareJ, { arg amp = 0.01, attack = 0.01, level = 1, gate = 1;
			var snare, snareenv, snareout, click, clickenv, clickout, claposc, clapenv, clapnoise, clapoutput, clap, snare2osc, snare2env, snare2noise, snare2noise2, snare2output;
			snare = SinOsc.ar(240);
			snareenv = EnvGen.kr(Env.perc(attack, 0.275, 2, -6), gate, level, doneAction:0.275);
			snareout = (snare * snareenv);
			click = {LPF.ar(WhiteNoise.ar(1),1200)};
			clickenv = {Line.ar(1, 0, 0.002) * level};
			clickout = (click * clickenv);
			clapnoise = {BPF.ar(LPF.ar(WhiteNoise.ar(1),7500),1500)};
			clapenv = {Line.ar(1, 0, 0.1, doneAction: 2)*level};
			clapoutput = {Mix.arFill(7, {arg i;
				EnvGen.ar(
					Env.new(
						[0,0,1,0],
						[0.01 * i,0,0.04]
					), gate
				) * clapnoise
				}
			)};
			snare2noise = {LPF.ar(WhiteNoise.ar(), 8000)};
			snare2noise2 = {LPF.ar(PinkNoise.ar(), 5000)};
			snare2osc = {HPF.ar(snare2noise + snare2noise2, 80)};
			snare2env = EnvGen.kr(Env.perc(attack,  0.2, 2, -6), gate, level,  doneAction:2);
			snare2output = (snare2osc * snare2env);
			clap = clapoutput * clapenv;
			Out.ar(0, Pan2.ar(clap + snareout + clickout + snare2output) * amp);
		}).store;

		SynthDef(\hat1J, { arg amp = 0.5, attack = 0.01, level =1, gate=1;
			var hatosc, hatenv, hatnoise, hatnoise2, hatoutput;
			hatnoise = {LPF.ar(WhiteNoise.ar(), 5000)};
			hatnoise2 = {LPF.ar(PinkNoise.ar(), 9000)};
			hatosc = {HPF.ar(hatnoise + hatnoise2, 8000)};
			hatenv = EnvGen.kr(Env.perc(attack,  0.1, level, -6), gate,  doneAction:2);
			hatoutput = (hatosc * hatenv);
			Out.ar(0,
				Pan2.ar(hatoutput) * amp
			)
		}).store;

		SynthDef(\tomJ, {arg amp = 0.5, level = 0.5, freq=430, attack = 0.001, gate = 1;
			var env4, tom, tomOut;
			env4 = EnvGen.kr(Env.perc(attack, 0.6, level, -5), gate, doneAction:2);
			tom = SinOsc.ar(freq);
			tomOut = Pan2.ar(tom*env4);
			Out.ar(0, FreeVerb.ar((tomOut* amp), 0.33, 0.5));
		}).store;

		SynthDef(\bufferTest, {arg out = 0, bufnum, rate = 1, loop = 1, start = 0, end = 0, trig = 1, amp = 0.2;
			var sound, output;
			sound = BufRd.ar(1, bufnum, Phasor.ar(start, rate, start, end));
			Out.ar(out, Pan2.ar(sound)*amp);
		}).store;

		SynthDef(\oneLoop, { |out, bufnum, start, time, amp|
			var    sig = PlayBuf.ar(1, bufnum, startPos: start, loop: 0),
			env = EnvGen.kr(Env.linen(0.01, time, 0.05, level: amp), doneAction: 2);
			Out.ar(out, (sig * env) ! 2)
		}).store;

		SynthDef(\solo1J, {arg freq = 440, oscType = 1, filtType = 1, envType = 0, cutofffreq = 500, rq = 0.3, attack = 0.1, decay = 3, sustain = 0, release = 3, envlevel = 1, curve = -6, start = 1.0, end = 0, dura = 0.5,  gate = 1, amp = 0.1, lforate = 1;
			var oscArray, envArray, filtArray, lfoArray, sound, env, root, lfo, endi, outi;
			lfo = SinOsc.ar(lforate);
			oscArray = [
				Mix(SinOsc.ar([([0.995, 1, 1.02, 1.5] * freq),], 0, 0.2)),
				Mix(Saw.ar([([0.995, 1, 1.02, 1.5] * freq),], 0.2)),
				Mix(Pulse.ar([([0.995, 1, 1.02, 1.5] * freq),], 0.2, 0.2)),
				Mix(LFTri.ar([([0.995, 1, 1.02, 1.5] * freq),], 0.2, 0.2))];
			filtArray = [
				Select.ar(oscType, oscArray), // Off
				RLPF.ar(Select.ar(oscType, oscArray), cutofffreq, rq),
				RHPF.ar(Select.ar(oscType, oscArray), cutofffreq, rq),
				MoogFF.ar(Select.ar(oscType, oscArray),  cutofffreq, rq)];
			envArray = [EnvGen.kr(Env.adsr(attack, decay, sustain, release), gate, doneAction:2),
				EnvGen.kr(Env.perc(attack, release, envlevel, curve), gate, doneAction:2),
				Line.kr(start, end, dura, doneAction:2)];
			env = Select.kr(envType, envArray) * lfo;
			sound = (Select.ar(filtType, filtArray));
			Out.ar(0, Pan2.ar(sound*env*amp));

		}).store;
	}
}
