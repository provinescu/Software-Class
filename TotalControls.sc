// A Software by Herve Provini

TotalControls {

	classvar s;

	var flagManualPlaying, wScore, menuScore, startManualScore, startTdefScore, routineScore, flagManualPlaying, scorePlaying, wEditScore, startsysteme, tempoMusicPlay, startsysteme, validScore, netScoreAddr, items, foncLoadSaveScore, commande, fonctionCommandes;
	var loopScore, windows, numView, fonctionCommandes2, startItems;

	*new	{arg path=nil;

		^super.new.init(path);

	}

	init	{arg path;

		// Setup GUI style
		QtGUI.palette = QPalette.dark;// light / system

		flagManualPlaying = 'off';
		// Standalone Robot = 57130, Agents = 57131, Matrix = 57132, Time = 57133, Density = 57134, WekRobot = 57135, WekAgents = 57136, WekMatrix = 57137, WekTime = 57138, WekDensity = 57139
		// Provinescu All Soft = 57140
		// SuperCollider
		netScoreAddr = netScoreAddr.add(NetAddr.new("127.0.0.1", NetAddr.langPort));
		// Standalone
		10.do({arg i;
			netScoreAddr = netScoreAddr.add(NetAddr.new("127.0.0.1", 57130+i));
		});
		// Provinescu
		netScoreAddr = netScoreAddr.add(NetAddr.new("127.0.0.1", 57140));

		items = 0;
		startItems = 0;
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
						scorePlaying = score.interpret;
						wEditScore.string_(score);
						file.close;
						//wScore.name="Score Editor/Player and ShortCuts Controls for HP Software " + path.asPathName.fileName;
						wScore.view.children.at(0).string = "Score Editor/Player and ShortCuts Controls for HP Software | " + path.asPathName.fileName;
					}, {"cancelled".postln});
				},
				// Save score
				2, {
					Dialog.savePanel({arg path;
						file=File(path++".scd","w");
						file.write(wEditScore.string);file.close;
						//wScore.name="Score Editor/Player and ShortCuts Controls for HP Software " + path.asPathName.fileName;
						wScore.view.children.at(0).string = "Score Editor/Player and ShortCuts Controls for HP Software | " + path.asPathName.fileName;
					},
					{"cancelled".postln});
				}
			);
		};

		//Score
		wScore = Window("TotalControls (Score Editor/Player and ShortCuts Controls for HP Software)", Rect(250, 250, 625, 525));
		wScore.view.decorator = FlowLayout(wScore.view.bounds);
		StaticText(wScore, Rect(0, 0, 620, 24)).string_("TotalControls (A Score Editor/Player and ShortCuts Controls)").stringColor_(Color.white(1.0,1.0));
		wScore.view.decorator.nextLine;
		// Load Score
		menuScore = PopUpMenu(wScore,Rect(0, 0, 110, 20)).background_(Color.grey(0.5, 0.8)).items = ["Score menu", "Load Score", "Save Score"];
		menuScore.action={arg item;
			foncLoadSaveScore.value(item.value);
			menuScore.value_(0);
			//items = 0;
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
				//items = 0;
			},
			{
				"Stop Tdef Score".postln;
				routineScore.value(scorePlaying).stop;
				routineScore.value(scorePlaying).clear;
				routineScore.value(scorePlaying).remove;
				// Play Score
				//items = 0;
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
				//items = 0;
				wScore.view.children.at(numView.value).focus;
			},
			{
				// Play Score
				//items = 0;
				"Start Tdef Score".postln;
				routineScore.value(scorePlaying).reset;
				routineScore.value(scorePlaying).play;
				wScore.view.children.at(numView.value).focus;
			});
		};
		validScore = Button(wScore,Rect(0, 0, 110, 20)).states=[["Val+Init Score", Color.black, Color.blue(0.8, 0.25)]];
		validScore.action = {arg view;
			items = 0;
			startItems = 0;
			wScore.view.children.at(8).children.at(1).value_(0);
			scorePlaying  = wEditScore.string.interpret;
			scorePlaying.postcs;
		};
		wScore.view.decorator.nextLine;

		// Ici les ShortCuts

		StaticText(wScore, Rect(0, 0, 50, 20)).string_("SCORE").stringColor_(Color.white(1.0,1.0));
		Button(wScore,Rect(0, 0, 250, 20)).states_([["Display Score Commandes + ShortCuts", Color.black, Color.red(0.8, 0.25)]]).
		action_(
			{
				TextView().name_("Commandes + ShortCuts").string_(
					"ShortCuts:

esc stop all soft
s play all soft
l Load preset for all soft
< or - next commande manual score

r robot preset
R wekrobot preset
alt+r robot start
ctrl+r robot stop
alt+R wekrobot start
ctrl+R wekrobot stop

a agent preset
A wekagent preset
alt+a agents start
ctrl+a agents stop
alt+A wekagents start
ctrl+A wekagents stop

m matrix preset
M wekmatrix preset
alt+m matrix start
ctrl+m matrix stop
alt+M wekmatrix start
ctrl+M wekmatrix stop

t time preset
T wektime preset
alt+t time start
ctrl+t time stop
alt+T wektime start
ctrl+T wektime stop

d density preset
D wekdensity preset
alt+d density start
ctrl+d density stop
alt+D wekdensity start
ctrl+D wekdensity stop

p provinescu preset
alt+p provinescu start
ctrl+p provinescu stop

Score Commandes:

'all'
'robot'
'wekrobot'
'agent'
'wekagent'
'matrix'
'wekmatrix'
'time'
'wektime'
'density'
'wekdensity'
'provinescu'
'preset'
'start'
'stop'
"
				).front;
			}
		);

		// Start Items Score
		EZNumber(wScore, 160@20, "Start Items Score", ControlSpec(0, 100, 'lin', 1),
			{arg i; items = i.value; startItems = i.value}, 0, true, 100, 50);
		wScore.view.decorator.nextLine;
		// Play Items Score
		TextView(wScore, Rect(0, 0, 600, 20)).string_("Current");
		//NumberBox(wScore, 35@20).value_(0);

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
						cmd = scorePlaying.at(items);
						{
							windows.value(5);
							wScore.view.children.at(9).string_("Next "++items.asString + scorePlaying.at(items));
						}.defer;
						scoreVal = [];
						if(cmd != nil,
							{
								// Time
								time = cmd.at(0);
								if(time  < 0.0417, {time=0.0417});
								// Commande
								cmd = cmd.at(1);
								val = cmd.at(item);
								while({val != nil},
									{
										if(val == 'all' or: {val == 'robot'} or: {val == 'agents'} or: {val == 'matrix'} or: {val == 'time'} or: {val == 'density'} or: {val == 'wekrobot'} or: {val == 'wekagents'} or: {val == 'wekmatrix'} or: {val == 'wektime'} or: {val == 'wekdensity'} or: {val == 'provinescu'},
											{
												scoreVal = scoreVal.add(val);
												val = cmd[item+1];
												scoreVal = scoreVal.add(val);
												// Preset
												if(val == 'preset',
													{
														number = cmd[item+2].asInteger;
														scoreVal = scoreVal.add(number);
														scoreVal.postcs;
														netScoreAddr.do({arg net; net.sendMsg(\score, *scoreVal)});
														{windows.value(5)}.defer(2);
														item = item + 3;
														scoreVal = [];
												});
												// Start Stop
												if(val == 'stop' or: {val == 'start'}, {
													scoreVal.postcs;
													netScoreAddr.do({arg net; net.sendMsg(\score, *scoreVal)});
													{windows.value(5)}.defer(2);
													item = item + 2;
													scoreVal = [];
												});
											},
											{
												"Bad or Unknow Command".postln;
												item = item + 1;
												/*items = startItems;
												{startTdefScore.valueAction_(0)}.defer;
												thisThread.stop;
												thisThread.remove;*/
											};
										);
										// Commande
										val = cmd.at(item);
									},
									{
										cmd = scorePlaying.at(items);
										// Time
										time = cmd.at(0);
										// Commande
										cmd = cmd.at(1);
										val = cmd.at(item);
								});
								items= items + 1;
								item = 0;
							},
							{
								if(loopScore == 'off',
									{
										items = startItems;
										item = 0;
										{startTdefScore.valueAction_(0)}.defer;
										thisThread.stop;
										thisThread.remove;
									},
									{
										items = startItems;
										item = 0;
								});
						});
						time.wait;
					});
			}).play;
		};

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

			// key < next score titem
			if(char == $< and: {modifiers == 0} or: {char == $- and: {modifiers == 0}},
				{
					if(flagManualPlaying == 'on',
						{
							cmd = scorePlaying.at(items);
							{
								windows.value(3);
								wScore.view.children.at(9).string_("Current "++items.asString + scorePlaying.at(items));
							}.defer;
							if(cmd != nil,
								{
									// Time
									time = cmd.at(0);
									// Commande
									cmd = cmd.at(1);
									val = cmd.at(item);
									while({val != nil},
										{
											if(val == 'all' or: {val == 'robot'} or: {val == 'agents'} or: {val == 'matrix'} or: {val == 'time'} or: {val == 'density'} or: {val == 'wekrobot'} or: {val == 'wekagents'} or: {val == 'wekmatrix'} or: {val == 'wektime'} or: {val == 'wekdensity'} or: {val == 'provinescu'},
												{
													scoreVal = scoreVal.add(val);
													val = cmd[item+1];
													scoreVal = scoreVal.add(val);
													// Preset
													if(val == 'preset',
														{
															number = cmd[item+2].asInteger;
															scoreVal = scoreVal.add(number);
															scoreVal.postcs;
															netScoreAddr.do({arg net; net.sendMsg(\score, *scoreVal)});
															{windows.value(3)}.defer(2);
															item = item + 3;
															scoreVal = [];
													});
													// Start Stop
													if(val == 'stop' or: {val == 'start'}, {
														scoreVal.postcs;
														netScoreAddr.do({arg net; net.sendMsg(\score, *scoreVal)});
														{windows.value(3)}.defer(2);
														item = item + 2;
														scoreVal = [];
													});
												},
												{
													"Bad or Unknow Command".postln;
													item = item + 1;
													//{startManualScore.valueAction_(0)}.defer;
												};
											);
											// Commande
											val = cmd.at(item);
										},
										{
											cmd = scorePlaying.at(items);
											// Time
											time = cmd.at(0);
											// Commande
											cmd = cmd.at(1);
											val = cmd.at(item);
									});
									items = items + 1;
									item = 0;
								},
								{
									if(loopScore == 'off',
										{
											{startManualScore.valueAction_(0)}.defer;
											flagManualPlaying = 'off';
											items = startItems;
											item = 0;
										},
										{
											items = startItems;
											item = 0;
									});
							});
					});
			});
			// key l -> load Preset for all soft
			if(char == $l and: {modifiers==0},
				{
					commande = ["all", "preset"];
			});
			// key esc -> stop all soft
			if(char == 27.asAscii and: {modifiers==0},
				{
					{windows.value(numView.value)}.defer(2);
					cmd=[];
					cmd =cmd.add("all");
					cmd = cmd.add("stop").postcs;
					netScoreAddr.do({arg net; net.sendMsg(\score, *cmd)});
					{windows.value(numView.value)}.defer(2);
			});
			// key s -> start all soft
			if(char == $s and: {modifiers==0},
				{
					{windows.value(numView.value)}.defer(2);
					cmd=[];
					cmd =cmd.add("all");
					cmd = cmd.add("start").postcs;
					netScoreAddr.do({arg net; net.sendMsg(\score, *cmd)});
					{windows.value(numView.value)}.defer(2);
			});
			// ROBOT
			// key r -> robot preset
			if(char == $r and: {modifiers==0},
				{
					commande = ["robot", "preset"];
			});
			// key R -> wekrobot preset
			if(char == $R and: {modifiers==131072},
				{
					commande = ["wekrobot", "preset"];
			});
			// key alt+r -> robot start
			if(char == $r and: {modifiers==524288},
				{
					commande = ["robot", "start"];
					fonctionCommandes2.value(commande);
			});
			// key ctrl+r -> robot stop
			if(char == 18.asAscii and: {modifiers==262144},
				{
					commande = ["robot", "stop"];
					fonctionCommandes2.value(commande);
			});
			// key alt+R -> wekrobot start
			if(char == $R and: {modifiers==655360},
				{
					commande = ["wekrobot", "start"];
					fonctionCommandes2.value(commande);
			});
			// key ctrl+R -> wekrobot stop
			if(char == 18.asAscii and: {modifiers==393216},
				{
					commande = ["wekrobot", "stop"];
					fonctionCommandes2.value(commande);
			});
			// AGENTS
			// key a -> agents preset
			if(char == $a and: {modifiers==0},
				{
					commande = ["agents", "preset"];
			});
			// key A -> wekagents preset
			if(char == $A and: {modifiers==131072},
				{
					commande = ["wekagents", "preset"];
			});
			// key alt+a -> agents start
			if(char == $a and: {modifiers==524288},
				{
					commande = ["agents", "start"];
					fonctionCommandes2.value(commande);
			});
			// key ctrl+a -> agents stop
			if(char == 1.asAscii and: {modifiers==262144},
				{
					commande = ["agents", "stop"];
					fonctionCommandes2.value(commande);
			});
			// key alt+A -> wekagents start
			if(char == $A and: {modifiers==655360},
				{
					commande = ["wekagents", "start"];
					fonctionCommandes2.value(commande);
			});
			// key ctrl+A -> wekagens stop
			if(char == 1.asAscii and: {modifiers==393216},
				{
					commande = ["wekagents", "stop"];
					fonctionCommandes2.value(commande);
			});
			// MATRIX
			// key m -> matrix preset
			if(char == $m and: {modifiers==0},
				{
					commande = ["matrix", "preset"];
			});
			// key M -> wekagent preset
			if(char == $M and: {modifiers==131072},
				{
					commande = ["wekmatrix", "preset"];
			});
			// key alt+m -> agent start
			if(char == $m and: {modifiers==524288},
				{
					commande = ["matrix", "start"];
					fonctionCommandes2.value(commande);
			});
			// key ctrl+m -> agent stop
			if(char == $\r and: {modifiers==262144},
				{
					commande = ["matrix", "stop"];
					fonctionCommandes2.value(commande);
			});
			// key alt+M -> wekagent start
			if(char == $M and: {modifiers==655360},
				{
					commande = ["wekmatrix", "start"];
					fonctionCommandes2.value(commande);
			});
			// key ctrl+M -> wekagent stop
			if(char == $\r and: {modifiers==393216},
				{
					commande = ["wekmatrix", "stop"];
					fonctionCommandes2.value(commande);
			});
			// TIME
			// key t -> time preset
			if(char == $t and: {modifiers==0},
				{
					commande = ["time", "preset"];
			});
			// key T -> wekagent preset
			if(char == $T and: {modifiers==131072},
				{
					commande = ["wektime", "preset"];
			});
			// key alt+t -> agent start
			if(char == $t and: {modifiers==524288},
				{
					commande = ["time", "start"];
					fonctionCommandes2.value(commande);
			});
			// key ctrl+t -> agent stop
			if(char == 20.asAscii and: {modifiers==262144},
				{
					commande = ["time", "stop"];
					fonctionCommandes2.value(commande);
			});
			// key alt+T -> wekagent start
			if(char == $T and: {modifiers==655360},
				{
					commande = ["wektime", "start"];
					fonctionCommandes2.value(commande);
			});
			// key ctrl+T -> wekagent stop
			if(char == 20.asAscii and: {modifiers==393216},
				{
					commande = ["wektime", "stop"];
					fonctionCommandes2.value(commande);
			});
			// DENSITY
			// key d -> density preset
			if(char == $d and: {modifiers==0},
				{
					commande = ["density", "preset"];
			});
			// key D -> wekagent preset
			if(char == $D and: {modifiers==131072},
				{
					commande = ["wekdensity", "preset"];
			});
			// key alt+d -> agent start
			if(char == $d and: {modifiers==524288},
				{
					commande = ["density", "start"];
					fonctionCommandes2.value(commande);
			});
			// key ctrl+d -> agent stop
			if(char == 4.asAscii and: {modifiers==262144},
				{
					commande = ["density", "stop"];
					fonctionCommandes2.value(commande);
			});
			// key alt+D -> wekagent start
			if(char == $D and: {modifiers==655360},
				{
					commande = ["wekdensity", "start"];
					fonctionCommandes2.value(commande);
			});
			// key ctrl+D -> wekagent stop
			if(char == 4.asAscii and: {modifiers==393216},
				{
					commande = ["wekdensity", "stop"];
					fonctionCommandes2.value(commande);
			});
			// PROVINESCU
			// key p -> provinescu preset
			if(char == $p and: {modifiers==0},
				{
					commande = ["provinescu", "preset"];
			});
			// key alt+p -> provinescu start
			if(char == $p and: {modifiers==524288},
				{
					commande = ["provinescu", "start"];
					fonctionCommandes2.value(commande);
			});
			// key ctrl+p -> provinescu stop
			if(char == 16.asAscii and: {modifiers==262144},
				{
					commande = ["provinescu", "stop"];
					fonctionCommandes2.value(commande);
			});
		};

		// Fonction Commandes
		fonctionCommandes = {arg commandeExecute, number;
			var cmd=[];
			{windows.value(numView.value)}.defer(2);
			cmd = commandeExecute.add(number).postcs;
			netScoreAddr.do({arg net; net.sendMsg(\score, *cmd)});
			{windows.value(numView.value)}.defer(2);
			commande = [];
		};
		// Fonction Commandes Special
		fonctionCommandes2 = {arg commandeExecute;
			{windows.value(numView.value)}.defer(2);
			commandeExecute.postcs;
			netScoreAddr.do({arg net; net.sendMsg(\score, *commandeExecute)});
			{windows.value(numView.value)}.defer(2);
			commande = [];
		}

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
