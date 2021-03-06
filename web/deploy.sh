# deploy script for the web front-end
# This file is responsible for preprocessing all TypeScript files, making sure
# all dependencies are up-to-date, and copying all necessary files into the
# web deploy directory.
# This is the resource folder where maven expects to find our files
TARGETFOLDER=../backend/src/main/resources
# This is the folder that we used with the Spark.staticFileLocation command
WEBFOLDERNAME=web
# step 1: make sure we have someplace to put everything.  We will delete the
#         old folder tree, and then make it from scratch
rm -rf $TARGETFOLDER
mkdir $TARGETFOLDER
mkdir $TARGETFOLDER/$WEBFOLDERNAME
# there are many more steps to be done.  For now, we will just copy an HTML file
cp html/index.html $TARGETFOLDER/$WEBFOLDERNAME

# step 2: update our npm dependencies
npm update

# step 3: copy javascript files
cp node_modules/jquery/dist/jquery.min.js $TARGETFOLDER/$WEBFOLDERNAME
cp node_modules/handlebars/dist/handlebars.min.js $TARGETFOLDER/$WEBFOLDERNAME

# step 4: compile TypeScript files
node_modules/typescript/bin/tsc app.ts --strict --outFile $TARGETFOLDER/$WEBFOLDERNAME/app.js

# step 5: concatenate css files and then copy css file
#cat style.css css/ElementList.css css/EditEntryForm.css css/NewEntryForm.css > $TARGETFOLDER/$WEBFOLDERNAME/style.css

# step 6: compile handlebars templates to the deploy folder
#node_modules/handlebars/bin/handlebars hb/ElementList.hb >> $TARGETFOLDER/$WEBFOLDERNAME/templates.js

# step 7: copy credentials.json into the resources folder
#cp credentials.json $TARGETFOLDER

# set up Jasmine
#node_modules/typescript/bin/tsc apptest.ts --strict --outFile $TARGETFOLDER/$WEBFOLDERNAME/apptest.js
#cp spec_runner.html $TARGETFOLDER/$WEBFOLDERNAME
#cp node_modules/jasmine-core/lib/jasmine-core/jasmine.css $TARGETFOLDER/$WEBFOLDERNAME
#cp node_modules/jasmine-core/lib/jasmine-core/jasmine.js $TARGETFOLDER/$WEBFOLDERNAME
#cp node_modules/jasmine-core/lib/jasmine-core/boot.js $TARGETFOLDER/$WEBFOLDERNAME
#cp node_modules/jasmine-core/lib/jasmine-core/jasmine-html.js $TARGETFOLDER/$WEBFOLDERNAME
