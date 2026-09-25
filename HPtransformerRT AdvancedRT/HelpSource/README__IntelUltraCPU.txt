NOUVEAU PRESET DE BASE
Intel Ultra CPU - Apprentissage rapide

OBJECTIF
Charge minimale sur Mac Intel avec adaptation rapide et generation active.

REGLAGES OPERATIONNELS
Traitement OSC: 20 Hz
Learn/N: 4
Details: OFF
RCU: OFF
Replay: 0
Rappel memoire: 0
Rappel trajectoire: 0
Fenetre generation: 2
Advanced RT: OFF

DEMARRAGE SANS STUDIO
(
~hpTR = HPtransformerRT.new;
~hpTR.setIntelUltraCPUFastLearn;
)

DEMARRAGE AVEC STUDIO
(
~hpTR = HPtransformerRT.new;
~studio = HPTransformerStudio.new(~hpTR, \hpTR).front;
)
Puis Dashboard > Presets generaux > Intel Ultra CPU - Apprentissage rapide.
Activez Mode Scene apres le reglage.

NOTE
Apprentissage rapide signifie ici adaptation forte a chaque evenement appris. Le Studio apprend un evenement sur quatre pour conserver une charge CPU faible. Pour davantage de vitesse d'adaptation, reduire Learn/N a 2; pour moins de CPU, augmenter a 8.
