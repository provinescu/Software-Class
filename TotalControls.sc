// A Software by Herve Provini

TotalControls {

	classvar s;

	var flagManualPlaying, wScore, menuScore, startManualScore, startTdefScore, routineScore, flagManualPlaying, scorePlaying, wEditScore, startsysteme, tempoMusicPlay, startsysteme, validScore, netScoreAddr, items, foncLoadSaveScore, commande, fonctionCommandes;
	var nextTime;

	*new	{arg path=nil;

		^super.new.init(path);

	}

	init	{arg path;

		//Server.default = s = Server("TotalControls", NetAddr("localhost", 57563), Server.default.options);

		// Setup GUI style
		QtGUI.palette = QPalette.dark;// light / system

		flagManualPlaying = 'off';
		// Standalone
		// Robot = 57130, Agents = 57131, Matrix = 57132, Time = 57133, Density = 57134, WekRobot = 57135, WekAgents = 57136, WekMatrix = 57137, WekTime = 57138, WekDensity = 57139
		// Provinescu
		// Robot = 57150, Agents = 57151, Matrix = 57152, Time = 57153, Density = 57154, WekRobot = 57155, WekAgents = 57156, WekMatrix = 57157, WekTime = 57158, WekDensity = 57159
		netScoreAddr = netScoreAddr.add(NetAddr.new("127.0.0.1", NetAddr.langPort));
		// Standalone
		10.do({arg i;
			netScoreAddr = netScoreAddr.add(NetAddr.new("127.0.0.1", 57130+i));
			//thisProcess.openUDPPort(57130+i);
		});
		// Provinescu
		10.do({arg i;
			netScoreAddr = netScoreAddr.add(NetAddr.new("127.0.0.1", 57150+i));
			//thisProcess.openUDPPort(57150+i);
		});

		items = 0;
		nextTime = 0;
		scorePlaying = [];

		this.edit;

	}

	edit {

		/*s.waitForBoot({
		nil;
		});*/

		foncLoadSaveScore = {arg flag;
			var path, file, score;
			flag.value.switch(
				// Do Nothing
				0, {nil},
				// Load score
				1, {
					Dialog.openPanel({ arg path;
						file=File(path,"r");
						score = file.readAllString;
						wScore.name="Score Editor/Player for HP Software "+ path;
						scorePlaying = score.interpret;
						wEditScore.string_(score);
						file.close;
					}, {"cancelled".postln});
				},
				// Save score
				2, {
					Dialog.savePanel({arg path;
						file=File(path++".scd","w");
						file.write(wEditScore.string);file.close;
					},
					{"cancelled".postln});
				}
			);
		};

		//Score
		wScore = Window("TotalControls (Score Editor/Player for HP Software)", Rect(250, 250, 625, 500));
		wScore.view.decorator = FlowLayout(wScore.view.bounds);
		StaticText(wScore, Rect(0, 0, 500, 24)).string_("TotalControls (A Score Editor/Player)").stringColor_(Color.white(1.0,1.0)).font_(Font("Georgia", 14));
		wScore.view.decorator.nextLine;
		// Load Score
		menuScore = PopUpMenu(wScore,Rect(0, 0, 125, 20)).background_(Color.grey(0.5, 0.8)).items = ["Score menu", "Load Score", "Save Score"];
		menuScore.action={arg item;
			foncLoadSaveScore.value(item.value);
			menuScore.value_(0);
		};
		menuScore.focus;
		// Button Score
		startManualScore = Button(wScore,Rect(10, 10, 125, 18)).states=[ ["Manual Stop Score", Color.black, Color.green(0.8, 0.25)], ["Manual Play Score", Color.black, Color.red(0.8, 0.25)]];
		startManualScore.action = {arg view;
			if(view.value == 0, {
				// Stop Score
				"Stop Manual Score".postln;
				flagManualPlaying = 'off';
				items = 0;
				nextTime = 0;
			},
			{
				// Play Score
				items = 0;
				"Start Manual Score".postln;
				flagManualPlaying = 'on';
			});
		};
		startTdefScore = Button(wScore,Rect(10, 10, 125, 18)).states=[["Tdef Stop Score", Color.black, Color.yellow(0.8, 0.25)], ["Tdef Play Score", Color.black, Color.red(0.8, 0.25)]];
		startTdefScore.action = {arg view;
			if(view.value == 0, {
				// Stop Score
				"Stop Tdef Score".postln;
				routineScore.value(scorePlaying).stop;
				routineScore.value(scorePlaying).clear;
				routineScore.value(scorePlaying).remove;
				items = 0;
				nextTime = 0;
			},
			{
				// Play Score
				items = 0;
				"Start Tdef Score".postln;
				routineScore.value(scorePlaying).reset;
				routineScore.value(scorePlaying).play;
			});
		};
		validScore = Button(wScore,Rect(0, 0, 125, 20)).states=[["Validation Score", Color.black, Color.blue(0.8, 0.25)]];
		validScore.action = {arg view;
			items = 0;
			flagManualPlaying = 'off';
			scorePlaying  = wEditScore.string.interpret;
			scorePlaying.postcs;
		};
		wScore.view.decorator.nextLine;
		StaticText(wScore, Rect(0, 0, 175, 24)).string_("Score").stringColor_(Color.white(1.0,1.0)).font_(Font("Georgia", 48));
		wScore.view.decorator.nextLine;
		wEditScore = TextView(wScore, Rect(0, 0, 600, 400));
		wEditScore.hasVerticalScroller_(true);
		wEditScore.hasHorizontalScroller_(true);
		wEditScore.autohidesScrollers_(true);
		wEditScore.resize_(5);
		wEditScore.string_("[[ 1, ['agents', 'preset', 1] ],
[ 2, ['agents', 'preset', 2, 'all', 'preset', 3, 'density', 'preset', 1] ],
[ 1, ['end', 0, 0] ]]");

		wScore.onClose_({});

		wScore.front;

		//Setup Font
		wScore.view.do({arg view;
			view.children.do({arg subView;
				subView.font = Font("Helvetica", 14);
			});
		});

		// PROCESSUS Read Score
		routineScore = {arg score;
			var time=0.0417, cmd, val;
			Tdef(\ScorePlay,
				{
					loop({
						// Items
						cmd = score.at(items);
						// Time
						time = cmd.at(0);
						if(time  < 0.0417, {time=0.0417});
						// Commande
						val = cmd.at(1);
						if(val.at(0) != 'end', {
							// les commandes
							netScoreAddr.do({arg net; net.sendMsg(\score, *val)});
						},
						{
							{startTdefScore.valueAction_(0)}.defer;
							thisThread.stop;
							thisThread.remove;
						});
						items = items + 1;
						time.wait;
					});
			}).play;
		};

		wScore.view.keyDownAction = {arg view, char, modifiers, unicode, keycode;

			var cmd, val, time;

			//[view, char, modifiers, unicode, keycode].postcs;

			// Touches pave numerique
			if(modifiers==2097152 and: {unicode==49} and: {keycode==83},{fonctionCommandes.value(commande, 1)});
			if(modifiers==2097152 and: {unicode==50} and: {keycode==84},{fonctionCommandes.value(commande, 2)});
			if(modifiers==2097152 and: {unicode==51} and: {keycode==85},{fonctionCommandes.value(commande, 3)});
			if(modifiers==2097152 and: {unicode==52} and: {keycode==86},{fonctionCommandes.value(commande, 4)});
			if(modifiers==2097152 and: {unicode==53} and: {keycode==87},{fonctionCommandes.value(commande, 5)});
			if(modifiers==2097152 and: {unicode==54} and: {keycode==88},{fonctionCommandes.value(commande, 6)});
			if(modifiers==2097152 and: {unicode==55} and: {keycode==89},{fonctionCommandes.value(commande, 7)});
			if(modifiers==2097152 and: {unicode==56} and: {keycode==91},{fonctionCommandes.value(commande, 8)});
			if(modifiers==2097152 and: {unicode==57} and: {keycode==92},{fonctionCommandes.value(commande, 9)});
			if(modifiers==2097152 and: {unicode==48} and: {keycode==82},{fonctionCommandes.value(commande, 10)});
			if(modifiers==2228224 and: {unicode==49} and: {keycode==83},{fonctionCommandes.value(commande, 11)});
			if(modifiers==2228224and: {unicode==50} and: {keycode==84},{fonctionCommandes.value(commande, 12)});
			if(modifiers==2228224and: {unicode==51} and: {keycode==85},{fonctionCommandes.value(commande, 13)});
			if(modifiers==2228224and: {unicode==52} and: {keycode==86},{fonctionCommandes.value(commande, 14)});
			if(modifiers==2228224and: {unicode==53} and: {keycode==87},{fonctionCommandes.value(commande, 15)});
			if(modifiers==2228224and: {unicode==54} and: {keycode==88},{fonctionCommandes.value(commande, 16)});
			if(modifiers==2228224and: {unicode==55} and: {keycode==89},{fonctionCommandes.value(commande, 17)});
			if(modifiers==2228224and: {unicode==56} and: {keycode==91},{fonctionCommandes.value(commande, 18)});
			if(modifiers==2228224and: {unicode==57} and: {keycode==92},{fonctionCommandes.value(commande, 19)});
			if(modifiers==2228224and: {unicode==48} and: {keycode==82},{fonctionCommandes.value(commande, 20)});
			if(modifiers==2621440 and: {unicode==49} and: {keycode==83},{fonctionCommandes.value(commande, 21)});
			if(modifiers==2621440 and: {unicode==50} and: {keycode==84},{fonctionCommandes.value(commande, 22)});
			if(modifiers==2621440 and: {unicode==51} and: {keycode==85},{fonctionCommandes.value(commande, 23)});
			if(modifiers==2621440 and: {unicode==52} and: {keycode==86},{fonctionCommandes.value(commande, 24)});
			if(modifiers==2621440 and: {unicode==53} and: {keycode==87},{fonctionCommandes.value(commande, 25)});
			if(modifiers==2621440 and: {unicode==54} and: {keycode==88},{fonctionCommandes.value(commande, 26)});
			if(modifiers==2621440 and: {unicode==55} and: {keycode==89},{fonctionCommandes.value(commande, 27)});
			if(modifiers==2621440 and: {unicode==56} and: {keycode==91},{fonctionCommandes.value(commande, 28)});
			if(modifiers==2621440 and: {unicode==57} and: {keycode==92},{fonctionCommandes.value(commande, 29)});
			if(modifiers==2621440 and: {unicode==48} and: {keycode==82},{fonctionCommandes.value(commande, 30)});
			if(modifiers==2752512and: {unicode==49} and: {keycode==83},{fonctionCommandes.value(commande, 31)});
			if(modifiers==2752512and: {unicode==50} and: {keycode==84},{fonctionCommandes.value(commande, 32)});
			if(modifiers==2752512and: {unicode==51} and: {keycode==85},{fonctionCommandes.value(commande, 33)});
			if(modifiers==2752512and: {unicode==52} and: {keycode==86},{fonctionCommandes.value(commande, 34)});
			if(modifiers==2752512and: {unicode==53} and: {keycode==87},{fonctionCommandes.value(commande, 35)});
			if(modifiers==2752512and: {unicode==54} and: {keycode==88},{fonctionCommandes.value(commande, 36)});
			if(modifiers==2752512and: {unicode==55} and: {keycode==89},{fonctionCommandes.value(commande, 37)});
			if(modifiers==2752512and: {unicode==56} and: {keycode==91},{fonctionCommandes.value(commande, 38)});
			if(modifiers==2752512and: {unicode==57} and: {keycode==92},{fonctionCommandes.value(commande, 39)});
			if(modifiers==2752512and: {unicode==48} and: {keycode==82},{fonctionCommandes.value(commande, 40)});
			// Touches clavier numerique
			if(modifiers==0 and: {unicode==49} and: {keycode==18},{fonctionCommandes.value(commande, 1)});
			if(modifiers==0 and: {unicode==50} and: {keycode==19},{fonctionCommandes.value(commande, 2)});
			if(modifiers==0 and: {unicode==51} and: {keycode==20},{fonctionCommandes.value(commande, 3)});
			if(modifiers==0 and: {unicode==52} and: {keycode==21},{fonctionCommandes.value(commande, 4)});
			if(modifiers==0 and: {unicode==53} and: {keycode==23},{fonctionCommandes.value(commande, 5)});
			if(modifiers==0 and: {unicode==54} and: {keycode==22},{fonctionCommandes.value(commande, 6)});
			if(modifiers==0 and: {unicode==55} and: {keycode==26},{fonctionCommandes.value(commande, 7)});
			if(modifiers==0 and: {unicode==56} and: {keycode==28},{fonctionCommandes.value(commande, 8)});
			if(modifiers==0 and: {unicode==57} and: {keycode==25},{fonctionCommandes.value(commande, 9)});
			if(modifiers==0 and: {unicode==48} and: {keycode==29},{fonctionCommandes.value(commande, 10)});
			if(modifiers==131072 and: {unicode==43} and: {keycode==18},{fonctionCommandes.value(commande, 11)});
			if(modifiers==131072 and: {unicode==34} and: {keycode==19},{fonctionCommandes.value(commande, 12)});
			if(modifiers==131072 and: {unicode==42} and: {keycode==20},{fonctionCommandes.value(commande, 13)});
			if(modifiers==131072 and: {unicode==231} and: {keycode==21},{fonctionCommandes.value(commande, 14)});
			if(modifiers==131072 and: {unicode==37} and: {keycode==23},{fonctionCommandes.value(commande, 15)});
			if(modifiers==131072 and: {unicode==38} and: {keycode==22},{fonctionCommandes.value(commande, 16)});
			if(modifiers==131072 and: {unicode==47} and: {keycode==26},{fonctionCommandes.value(commande, 17)});
			if(modifiers==131072 and: {unicode==40} and: {keycode==28},{fonctionCommandes.value(commande, 18)});
			if(modifiers==131072 and: {unicode==41} and: {keycode==25},{fonctionCommandes.value(commande, 19)});
			if(modifiers==131072 and: {unicode==61} and: {keycode==29},{fonctionCommandes.value(commande, 20)});
			if(modifiers==524288 and: {unicode==177} and: {keycode==18},{fonctionCommandes.value(commande, 21)});
			if(modifiers==524288 and: {unicode==8220} and: {keycode==19},{fonctionCommandes.value(commande, 22)});
			if(modifiers==524288 and: {unicode==35} and: {keycode==20},{fonctionCommandes.value(commande, 23)});
			if(modifiers==524288 and: {unicode==199} and: {keycode==21},{fonctionCommandes.value(commande, 24)});
			if(modifiers==524288 and: {unicode==91} and: {keycode==23},{fonctionCommandes.value(commande, 25)});
			if(modifiers==524288 and: {unicode==93} and: {keycode==22},{fonctionCommandes.value(commande, 26)});
			if(modifiers==524288 and: {unicode==124} and: {keycode==26},{fonctionCommandes.value(commande, 27)});
			if(modifiers==524288 and: {unicode==123} and: {keycode==28},{fonctionCommandes.value(commande, 28)});
			if(modifiers==524288 and: {unicode==125} and: {keycode==25},{fonctionCommandes.value(commande, 29)});
			if(modifiers==524288 and: {unicode==8800} and: {keycode==29},{fonctionCommandes.value(commande, 30)});
			if(modifiers==655360 and: {unicode==8734} and: {keycode==18},{fonctionCommandes.value(commande, 31)});
			if(modifiers==655360 and: {unicode==8221} and: {keycode==19},{fonctionCommandes.value(commande, 32)});
			if(modifiers==655360 and: {unicode==8249} and: {keycode==20},{fonctionCommandes.value(commande, 33)});
			if(modifiers==655360 and: {unicode==8260} and: {keycode==21},{fonctionCommandes.value(commande, 34)});
			if(modifiers==655360 and: {unicode==91} and: {keycode==23},{fonctionCommandes.value(commande, 35)});
			if(modifiers==655360 and: {unicode==93} and: {keycode==22},{fonctionCommandes.value(commande, 36)});
			if(modifiers==655360 and: {unicode==92} and: {keycode==26},{fonctionCommandes.value(commande, 37)});
			if(modifiers==655360 and: {unicode==210} and: {keycode==28},{fonctionCommandes.value(commande, 38)});
			if(modifiers==655360 and: {unicode==212} and: {keycode==25},{fonctionCommandes.value(commande, 39)});
			if(modifiers==655360 and: {unicode==218} and: {keycode==29},{fonctionCommandes.value(commande, 40)});

			// key enter next score titem
			if(unicode == 13 and: {keycode == 36},
				{
					if(flagManualPlaying == 'on',
						{
							// Items
							cmd = scorePlaying.at(items);
							// Time
							time = cmd.at(0);
							// Commande
							val = cmd.at(1);
							if(val.at(0) != 'end', {
								// les commandes
								netScoreAddr.do({arg net; net.sendMsg(\score, *val)});
							},
							{
								startManualScore.valueAction_(0);
							});
							items = items + 1;
					});
			});
			// key l -> load Preset
			if(char == $l,
				{commande = 'load preset'});
			// key o -> load Preset
			if(char == $o,
				{
					cmd =cmd.add("all");
					cmd = cmd.add("stop");
					cmd = cmd.add(0);
					netScoreAddr.do({arg net; net.sendMsg(\score, *cmd)});
			});
			// key o -> load Preset
			if(char == $p,
				{
					cmd =cmd.add("all");
					cmd = cmd.add("start");
					cmd = cmd.add(0);
					netScoreAddr.do({arg net; net.sendMsg(\score, *cmd)});
			});
		};

		// Fonction Commandes
		fonctionCommandes = {arg commandeExecute, number;
			var cmd=[];
			//load Preset
			if(commandeExecute == 'load preset',
				{
					cmd =cmd.add("all");
					cmd = cmd.add("preset");
					cmd = cmd.add(number);
					netScoreAddr.do({arg net; net.sendMsg(\score, *cmd)});
			});
		};

		/*// OSCFunc Score
		OSCFunc.newMatching({arg msg, time, addr, recvPort;
			var cmd = 'on', item=0;
			[msg, time, addr, recvPort].postcs;
			msg.removeAt(0);
			/*while({cmd != nil},
				{
					cmd = msg[item].postln;
					cmd = msg[item+1].postln;
					cmd = msg[item+2].postln;
					"send".postcs;
					item = item + 3;
					cmd = msg[item];
			});*/
		}, \score, recvPort: NetAddr.langPort);*/

	}

}
