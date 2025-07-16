TestMultiCanals {


	classvar  <> s;

	var w, p, g, n;

	*new	{arg out=16, devIn="MutliTracks Recording", devOut="MutliTracks Recording";

		^super.new.init(out, devIn, devOut);

	}

	init	{arg out, devIn, devOut;

		s = Server.default;
		s.options.memSize = 2**20;
		s.options.inDevice = devIn;
		s.options.outDevice = devOut;
		//s.options.device = "JackRouter";// use a specific soundcard
		//s.options.device = "StreamDrums LoopBack";// use a specific soundcard
		//s.options.sampleRate = nil;//use the currently selected samplerate of the select hardware
		n = out;
		s.options.numOutputBusChannels_(n);

		s.waitForBoot({

			/*g = Group.new(s, \addToTail);
			s.sync;*/

			// Init Synth
			this.initSynthDef;
			s.sync;

			// Synth
			p = Synth.new("Noise");
			s.sync;

			// Create GUI Panel
			this.panel;
			s.sync;

			// Stop programme
			~cmdperiodfunc = {
				w.close;
				s.quit;
			};

			CmdPeriod.doOnce(~cmdperiodfunc);

			s.queryAllNodes;

		});

	}

	// Creation GUI

	panel {

		// MasterFX
		w = Window.new("TestMultiCanals", Rect(100, 500, 200, 200), scroll: true);
		w.view.decorator = FlowLayout(w.view.bounds);
		EZKnob(w, 80 @ 80, "Pan",\pan,
			{|pan| p.set(\pan, pan.value)}, 0, labelWidth: 60, unitWidth: 0, layout: 'vert');
		EZKnob(w, 80 @ 80, "Width",ControlSpec.new(0, 16),
			{|wid| p.set(\w, wid.value)}, 2, labelWidth: 60, unitWidth: 0, layout: 'vert');
		EZKnob(w, 80 @ 80, "Orientation",ControlSpec.new(0, n),
			{|ori| p.set(\o, ori.value)}, 0.5, labelWidth: 60, unitWidth: 0, layout: 'vert');
		w.front;
	}

	initSynthDef {

		SynthDef("Noise",
			{arg out = 0, amp = 0.25;

				var main, ambisonic, circle;

				circle = LFSaw.kr(1);

				// Main Synth
				main = PinkNoise.ar(amp);

				/*// Ambisonic
				ambisonic = PanB2.ar(main, \pan.kr(0));
				main = DecodeB2.ar(n, ambisonic[0], ambisonic[1], ambisonic[2], \o.kr(0.5) - n);

				*/

				// Multi Canals
				//main = PanAz.ar(n, main, circle, width: 1.0);
				main = PanAz.ar(n, main, \pan.kr(0), 1, \w.kr(2), \o.kr(0.5));// 3.5 pour front derriere moi

				// Out
				Out.ar(out, main);

		}).send(s);

	}

}