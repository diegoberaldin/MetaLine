<p align="center"><img width="300" src="https://github.com/diegoberaldin/MetaLine/assets/2738294/e987fae9-7c4b-41fd-a1fc-5a99d8fb1ccc" /></p>

# MetaLine

MetaLine is a companion tool to help translators create their own translation memories (TMs) from already translated texts and bilingual corpora. Translation memories can be exported into a TMX file that can be used with other CAT tools to retrieve fuzzy and exact matches.
This project is still under active development so do not expect a finished and polished application. There are some missing features and I'm currently working on extending the input formats, support SRX segmentation rules, improve the user experience and so forth…

## Main functions

### Welcome screen

<p align="center">
<img width="912" alt="welcome_screen" src="https://github.com/diegoberaldin/MetaLine/assets/2738294/dec48fe8-7ac8-4e78-b9b1-04196530805d" />
</p>

This screens contains a series of shortcuts to all the created projects. Projects that are not needed any more can be deleted with the corresponding button

### Project startup screen

<p align="center">
<img width="912" alt="project_screen" src="https://github.com/diegoberaldin/MetaLine/assets/2738294/55669379-e9a6-4e58-9163-f87701a88a30" />
</p>

This screen contains an entry for each file pair of the current project. The list of file pairs is crucial and can always be accessed from the sidebar. By double-clicking and entry in the "Project files" sidebar, or by selecting a file pair from the project startup screen, the corresponding list of segments will be opened in the main alignment window.

### Project creation

<p align="center">
<img width="912" alt="create_project_dialog" src="https://github.com/diegoberaldin/MetaLine/assets/2738294/10abb69b-7610-486e-97f3-16e2511078c7" />
</p>

This dialog allows you to create a new project by specifying a name, the source and target languages and the file pairs. It is a wizard where in the second step the custom segmentation rules will be configurable. 

### Project edit

<p align="center">
<img width="912" alt="project_settings_metadata" src="https://github.com/diegoberaldin/MetaLine/assets/2738294/d7ec2060-b5ab-44fe-a82c-ffb24692139a" />
 </p>

This is similar to project creation, except for allowing to modify the current project instead of creating a new one, e.g. to change the name or to add new file pairs.

<p>
<img width="912" alt="project_settings_segmentation" src="https://github.com/diegoberaldin/MetaLine/assets/2738294/82270309-8f57-43d0-bf45-b5d801f035eb" />
 </p>

Additionally, it is possible to configure the segmentation rules. The "Override default" allows to specify custom rules just for the current project, rather than relying on the global default ones.

### Alignment screen

<p align="center">
<img width="912" alt="alignment_screen" src="https://github.com/diegoberaldin/MetaLine/assets/2738294/680ec49e-5c81-446d-8da6-d2b3c678a593" />
</p>

This is the main screen where most of the alignment activity will take place. Note that each item in the main toolbar has a corresponding shortcut (please refer to the "Segment" menu in the application menu). Working with shortcuts can make the alignment work much faster and easier. 

### Project statistics

<p align="center">
<img width="712" alt="statistics" src="https://github.com/diegoberaldin/MetaLine/assets/2738294/0c1764a3-ac88-46fa-a488-a97540851337" />
</p>

This dialog contains a recap of the alignment progress, with a completion bar for each file pair and the total number of Translation Units (TUs) that will be included in the final memory.


### Settings

<p align="center">
<img width="712" alt="settings" src="https://github.com/diegoberaldin/MetaLine/assets/2738294/a39c650c-033c-4612-b44f-c27e552457d3" />
</p>

This dialog allows you to change the application language and displays the application version. In future releases, it will be possibile to configure font size and some of the application colors.

<p align="center">
<img width="712" alt="settings_segmentation" src="https://github.com/diegoberaldin/MetaLine/assets/2738294/e927f9ed-30bd-442e-a730-5417acff6613" />
 </p>
 
More importantly, this dialog allows you to configure the defult segmentation rules that will be applied for each project depending on the language.
Each language is identified by a pattern before the interruption/exception, a pattern after and whether this is an interruption or an exception rule. The precedence of the rules corresponds to the order in which they are displayed, with topmost rules having higher priority over the ones below.

## Trivia

MetaLine is part of a greater project of open source translation tools, e.g. [MetaTerm](https://github.com/diegoberaldin/MetaTerm) for terminology management. The "meta-" prefix comes from the Greek verb μεταφράζω meaning "to translate".
