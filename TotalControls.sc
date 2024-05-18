// A Software by Herve Provini

TotalControls {

	classvar s;

	var flagManualPlaying, wScore, menuScore, startManualScore, startTdefScore, routineScore, flagManualPlaying, scorePlaying, wEditScore, startsysteme, tempoMusicPlay, startsysteme, validScore, netScoreAddr, items, foncLoadSaveScore, commande, fonctionCommandes;
	var nextTime, loopScore, windows, numView;

	*new	{arg path=nil;

		^super.new.init(path);

	}

	init	{arg path;

		// Setup GUI style
		QtGUI.palette = QPalette.dark;// light / system

		flagManualPlaying = 'off';
		// Standalone Robot = 57130, Agents = 57131, Matrix = 57132, Time = 57133, Density = 57134, WekRobot = 57135, WekAgents = 57136, WekMatrix = 57137, WekTime = 57138, WekDensity = 57139
		// Provinescu All Soft = 57140
		netScoreAddr = netScoreAddr.add(NetAddr.new("127.0.0.1", NetAddr.langPort));
		// Standalone
		10.do({arg i;
			netScoreAddr = netScoreAddr.add(NetAddr.new("127.0.0.1", 57130+i));
		});
		// Provinescu
		netScoreAddr = netScoreAddr.add(NetAddr.new("127.0.0.1", 57140));

		items = 0;
		nextTime = 0;
		scorePlaying = [];
		loopScore = 'off';
		numView = 5;// valid score

		this.edit;

	}

	edit {

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
						wScore.name="Score Editor/Player and ShortCut Control for HP Software "+ path;
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
		wScore = Window("TotalControls (Score Editor/Player and ShortCut Control for HP Software)", Rect(250, 250, 625, 500));
		wScore.view.decorator = FlowLayout(wScore.view.bounds);
		StaticText(wScore, Rect(0, 0, 500, 24)).string_("TotalControls (A Score Editor/Player and ShortCut Control)").stringColor_(Color.white(1.0,1.0));
		wScore.view.decorator.nextLine;
		// Load Score
		menuScore = PopUpMenu(wScore,Rect(0, 0, 110, 20)).background_(Color.grey(0.5, 0.8)).items = ["Score menu", "Load Score", "Save Score"];
		menuScore.action={arg item;
			foncLoadSaveScore.value(item.value);
			menuScore.value_(0);
		};
		menuScore.focus;
		// Button Score Loop Score
		Button(wScore,Rect(10, 10, 105, 18)).states_([ ["Loop Score On", Color.black, Color.green(0.8, 0.25)], ["Loop Score Off", Color.black, Color.red(0.8, 0.25)]]).action_({arg view;
			if(view.value == 0, {
				"Loop Score Off".postln;
				loopScore = 'off';
				wScore.view.children.at(numView.value).focus;
			},
			{
				"Loop Score On".postln;
				loopScore = 'on';
				wScore.view.children.at(numView.value).focus;
			});
		});
		// Button Score
		startManualScore = Button(wScore,Rect(10, 10, 125, 18)).states=[ ["Manual Stop Score", Color.black, Color.green(0.8, 0.25)], ["Manual Play Score", Color.black, Color.red(0.8, 0.25)]];
		startManualScore.action = {arg view;
			numView = 3;// manual score
			startTdefScore.value_(0);
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
				wScore.view.children.at(numView.value).focus;
			});
		};
		startTdefScore = Button(wScore,Rect(10, 10, 110, 18)).states=[["Tdef Stop Score", Color.black, Color.yellow(0.8, 0.25)], ["Tdef Play Score", Color.black, Color.red(0.8, 0.25)]];
		startTdefScore.action = {arg view;
			numView = 5;// valid score
			startManualScore.value_(0);
			if(view.value == 0, {
				// Stop Score
				"Stop Tdef Score".postln;
				routineScore.value(scorePlaying).stop;
				routineScore.value(scorePlaying).clear;
				routineScore.value(scorePlaying).remove;
				items = 0;
				nextTime = 0;
				wScore.view.children.at(numView.value).focus;
			},
			{
				// Play Score
				items = 0;
				"Start Tdef Score".postln;
				routineScore.value(scorePlaying).reset;
				routineScore.value(scorePlaying).play;
				wScore.view.children.at(numView.value).focus;
			});
		};
		validScore = Button(wScore,Rect(0, 0, 110, 20)).states=[["Validation Score", Color.black, Color.blue(0.8, 0.25)]];
		validScore.action = {arg view;
			items = 0;
			flagManualPlaying = 'off';
			scorePlaying  = wEditScore.string.interpret;
			scorePlaying.postcs;
		};
		wScore.view.decorator.nextLine;
		StaticText(wScore, Rect(0, 0, 175, 24)).string_("SCORE").stringColor_(Color.white(1.0,1.0));
		wScore.view.decorator.nextLine;
		wEditScore = TextView(wScore, Rect(0, 0, 600, 400));
		wEditScore.hasVerticalScroller_(true);
		wEditScore.hasHorizontalScroller_(true);
		wEditScore.autohidesScrollers_(true);
		wEditScore.resize_(5);
		wEditScore.font_(Font("Time", 14));
		wEditScore.string_("[
[ 1, ['all', 'stop'] ],

[ 2, ['all', 'preset', 1, 'agents', 'start',] ],
[ 2, ['matrix', 'start',] ],
[ 2, ['wektime', 'start'] ],

[ 1, ['all', 'stop'] ],

[ 2, ['all', 'preset', 2, 'wektime', 'start',] ],
[ 2, ['agents', 'start',] ],
[ 2, ['matrix', 'start'] ],

[ 1, ['all', 'stop'] ],

[ 2, ['all', 'preset', 3, 'agents', 'start',] ],
[ 2, ['matrix', 'start',] ],
[ 2, ['wektime', 'start'] ],

[ 1, ['all', 'stop'] ],

]");

		wScore.onClose_({});

		wScore.front;

		// PROCESSUS Read Score
		routineScore = {arg score;
			var time=0.0417, cmd=[], val, item = 0, scoreVal = [], number;
			Tdef(\ScorePlay,
				{
					loop({
						{windows.value(3)}.defer(2);
						cmd = scorePlaying.at(items).postcs;
						if(cmd != nil,
							{
								// Time
								time = cmd.at(0).postcs;
								if(time  < 0.0417, {time=0.0417});
								// Commande
								cmd = cmd.at(1).postcs;
								val = cmd.at(item).postcs;
								while({val != nil},
									{
										if(val == 'all' or: {val == 'robot'} or: {val == 'agents'} or: {val == 'matrix'} or: {val == 'time'} or: {val == 'density'} or: {val == 'wekrobot'} or: {val == 'wekagents'} or: {val == 'wekmatrix'} or: {val == 'wektime'} or: {val == 'wekdensity'},
											{
												scoreVal = scoreVal.add(val);
												val = cmd[item+1].postln;
												scoreVal = scoreVal.add(val);
												// Preset
												if(val == 'preset',
													{
														number = cmd[item+2].asInteger.postln;
														scoreVal = scoreVal.add(number);
														netScoreAddr.do({arg net; net.sendMsg(\score, *scoreVal)});
														{windows.value(3)}.defer(2);
														item = item + 3;
												});
												// Start Stop
												if(val == 'stop' or: {val == 'start'}, {
													netScoreAddr.do({arg net; net.sendMsg(\score, *scoreVal)});
													{windows.value(3)}.defer(2);
													item = item + 2;
												});
											},
											{
												"Bad Command".postln;
												{startManualScore.valueAction_(0)}.defer(2);
											};
										);
										// Commande
										val = cmd.at(item);
									},
									{
										cmd = scorePlaying.at(items).postcs;
										// Time
										time = cmd.at(0).postcs;
										// Commande
										cmd = cmd.at(1).postcs;
										val = cmd.at(item).postcs;
								});
								items= items + 1;
								item = 0;
							},
							{
								if(loopScore == 'off',
									{
										{startManualScore.valueAction_(0)}.defer(2);
										thisThread.stop;
										thisThread.remove;
									},
									{
										items = 0;
										item = 0;
								});
						});
						time.wait;
					});
			}).play;
		};
		/////////////////

		/*loop({
		{windows.value(numView)}.defer(2);
		// Items
		cmd = score.at(items).postcs;
		if(cmd != nil,
		{
		// Time
		time = cmd.at(0);
		if(time  < 0.0417, {time=0.0417});
		// Commande
		val = cmd.at(1);
		netScoreAddr.do({arg net; net.sendMsg(\score, *val)});
		items = items + 1;
		{windows.value(numView)}.defer(2);
		time.wait;
		},
		{
		if(loopScore == 'off',
		{
		{startTdefScore.valueAction_(0)}.defer(2);
		thisThread.stop;
		thisThread.remove;
		},
		{
		items = 0;
		});
		});
		});
		}).play;
		};*/

		windows = {arg num;
			wScore.front;
			wScore.view.children.at(num.value).focus;
			wScore.front;
		};

		wScore.view.children.at(5).valueAction_(1);
		wScore.view.children.at(5).focus;

		wScore.view.keyDownAction = {arg view, char, modifiers, unicode, keycode;

			var val, time, cmd = 'on', item = 0, scoreVal=[], number;

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
							{windows.value(3)}.defer(2);

							/////
							cmd = scorePlaying.at(items).postcs;
							if(cmd != nil,
								{
									// Time
									time = cmd.at(0).postcs;
									// Commande
									cmd = cmd.at(1).postcs;
									val = cmd.at(item).postcs;
									while({val != nil},
										{
											if(val == 'all' or: {val == 'robot'} or: {val == 'agents'} or: {val == 'matrix'} or: {val == 'time'} or: {val == 'density'} or: {val == 'wekrobot'} or: {val == 'wekagents'} or: {val == 'wekmatrix'} or: {val == 'wektime'} or: {val == 'wekdensity'},
												{
													scoreVal = scoreVal.add(val);
													val = cmd[item+1].postln;
													scoreVal = scoreVal.add(val);
													// Preset
													if(val == 'preset',
														{
															number = cmd[item+2].asInteger.postln;
															scoreVal = scoreVal.add(number);
															netScoreAddr.do({arg net; net.sendMsg(\score, *scoreVal)});
															{windows.value(3)}.defer(2);
															item = item + 3;
													});
													// Start Stop
													if(val == 'stop' or: {val == 'start'}, {
														netScoreAddr.do({arg net; net.sendMsg(\score, *scoreVal)});
														{windows.value(3)}.defer(2);
														item = item + 2;
													});
												},
												{
													"Bad Command".postln;
													{startManualScore.valueAction_(0)}.defer(2);
												};
											);
											// Commande
											val = cmd.at(item);
										},
										{
											cmd = scorePlaying.at(items).postcs;
											// Time
											time = cmd.at(0).postcs;
											// Commande
											cmd = cmd.at(1).postcs;
											val = cmd.at(item).postcs;
									});
								},
								{
									if(loopScore == 'off',
										{
											{startManualScore.valueAction_(0)}.defer(2);
											items = 0;
										},
										{
											items = 0;
									});
							});
							items = items + 1;
							/////

							// Old Version
							/*// Items
							cmd = scorePlaying.at(items).postcs;
							if(cmd != nil,
							{
							// Time
							time = cmd.at(0);
							// Commande
							val = cmd.at(1);
							netScoreAddr.do({arg net; net.sendMsg(\score, *val)});
							{windows.value(3)}.defer(2);
							items = items + 1;
							},
							{
							if(loopScore == 'off',
							{
							startManualScore.valueAction_(0);
							},
							{
							items = 0;
							// Items
							cmd = scorePlaying.at(items).postcs;
							// Time
							time = cmd.at(0);
							// Commande
							val = cmd.at(1);
							netScoreAddr.do({arg net; net.sendMsg(\score, *val)});
							{windows.value(3)}.defer(2);
							items = items + 1;
							});
							});*/
					});
			});
			// key l -> load Preset
			if(char == $l,
				{commande = 'load preset'});
			// key o -> stop soft
			if(char == $o,
				{
					{windows.value(numView.value)}.defer(2);
					cmd =cmd.add("all");
					cmd = cmd.add("stop");
					cmd = cmd.add(0).postcs;
					netScoreAddr.do({arg net; net.sendMsg(\score, *cmd)});
					{windows.value(numView.value)}.defer(2);
			});
			// key o -> start soft
			if(char == $p,
				{
					{windows.value(numView.value)}.defer(2);
					cmd =cmd.add("all");
					cmd = cmd.add("start");
					cmd = cmd.add(0).postcs;
					netScoreAddr.do({arg net; net.sendMsg(\score, *cmd)});
					{windows.value(numView.value)}.defer(2);
			});
		};

		// Fonction Commandes
		fonctionCommandes = {arg commandeExecute, number;
			var cmd=[];
			//load Preset
			if(commandeExecute == 'load preset',
				{

					{windows.value(numView.value)}.defer(2);
					cmd =cmd.add("all");
					cmd = cmd.add("preset");
					cmd = cmd.add(number).postcs;
					netScoreAddr.do({arg net; net.sendMsg(\score, *cmd)});
					{windows.value(numView.value)}.defer(2);
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
