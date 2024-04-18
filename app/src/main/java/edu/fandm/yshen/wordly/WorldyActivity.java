package edu.fandm.yshen.wordly;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;
import java.util.Stack;
import java.util.Vector;

public class WorldyActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String FIRST_LAUNCH_KEY = "firstLaunch";

    ShortestPath shortestPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worldy);

        View rootView = findViewById(android.R.id.content);
        rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.hide();
        }

        if (isFirstLaunch()) {
            // Set the flag to indicate that the app has been launched
            setFirstLaunchFlag(false);
            Intent i = new Intent(getApplicationContext(),FirstLaunchActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }

        //create graph
        Graph<String> graph = null;
        try {
            graph = createGraphFromAsset("words_simple.txt");
            //System.out.println(graph.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] rand = getRandomWords(graph.getNodes(), graph);


        EditText start_et = (EditText) findViewById(R.id.start_et);
        start_et.setText(rand[0]);
        start_et.setFilters(new InputFilter[]{new EnglishWordsInputFilter()});

        EditText end_et = (EditText) findViewById(R.id.end_et);
        end_et.setText(rand[1]);
        end_et.setFilters(new InputFilter[]{new EnglishWordsInputFilter()});

        Button play_bt = (Button) findViewById(R.id.play_bt);
        Graph<String> finalGraph = graph;
        play_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String start = start_et.getText().toString();
                String end = end_et.getText().toString();
                if(checkInput(finalGraph, start)){
                    shortestPath = new ShortestPath(finalGraph, start);
                    if(checkInput(finalGraph, end)){
                        ArrayList<String> path = shortestPath.getPath(end);
                        if(path.size() < 9){
                            System.out.println(path.toString());
                            launchPlay(v, path);
                        }else{
                            end_et.setText("");
                        }
                    }
                }

            }
        });

        Button newGame_bt = (Button) findViewById(R.id.new_bt);
        newGame_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] rand = getRandomWords(finalGraph.getNodes(), finalGraph);
                start_et.setText(rand[0]);
                end_et.setText(rand[1]);
            }
        });
    }


    private boolean isFirstLaunch() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(FIRST_LAUNCH_KEY, true);
    }

    private void setFirstLaunchFlag(boolean value) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(FIRST_LAUNCH_KEY, value);
        editor.apply();
    }


    //creating graph

    private Graph<String> createGraphFromAsset(String filename) throws IOException {
        AssetManager assetManager = getAssets();
        InputStream inputStream = null;

        try {
            inputStream = assetManager.open(filename);
            Graph<String> graph = createGraph(inputStream);

            return graph;
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Graph<String> createGraph(InputStream inputStream){
        Scanner scanner = new Scanner(inputStream);
        Graph<String> graph = new Graph<>();
        String file_input = scanner.nextLine();
        while(file_input != null){
            if(file_input.length() == 4){
                if(graph.getVertexCount() == 0){
                    graph.addVertex(file_input);
                }else{
                    ArrayList<String> nodes = graph.getNodes();
                    addNode(nodes, file_input, graph);
                }
            }
            try{
                file_input = scanner.nextLine();
            }catch (NoSuchElementException nsee){
                nsee.printStackTrace();
                break;
            }

        }
        return graph;
    }

    private void addNode(ArrayList<String> nodes, String str, Graph graph){
        boolean added = false;
        for(String s:nodes){
            if(oneLetterDiff(s,str)){
                //Log.d("addnode onediff",s+" "+str);
                graph.addEdge(s,str,true);
                added = true;
            }
        }
        if(!added){
            graph.addVertex(str);
        }
    }

    private boolean oneLetterDiff(String s1, String s2){
        if(s1.length() != s2.length()){
            return false;
        }
        int diffCount = 0;
        for(int i = 0; i < s1.length(); i++){
            if(s1.charAt(i) != s2.charAt(i)){
                diffCount++;
            }
        }
        return diffCount == 1;
    }

    //breath first search & shortest path
    public class ShortestPath{
        private Graph<String> g;
        private String start;
        //check if a node is visited, key: node, value: boolean
        private Map<String, Boolean> visited;
        //record path, key: this node, value: last node
        private Map<String, String> from;
        //record order, key: node, value: how many steps taken
        private Map<String, Integer> ord;
        //constructor
        public ShortestPath(Graph<String> graph, String str){
            //initialize
            g = graph;
            visited = new HashMap<>();
            from = new HashMap<>();
            ord = new HashMap<>();
            ArrayList<String> nodes = g.getNodes();
            for(String node:nodes){
                visited.put(node,false);
                from.put(node, null);
                ord.put(node, -1);
            }
            start = str;

            //breath first search
            LinkedList<String> queue = new LinkedList<>();
            queue.push(start);
            visited.replace(start,true);
            ord.replace(start,0);
            while(!queue.isEmpty()){
                String currNode = queue.pop();
                for(String temp : g.adj(currNode)){
                    if(!visited.get(temp)){
                        queue.push(temp);
                        visited.replace(temp,true);
                        from.replace(temp,currNode);
                        ord.replace(temp,ord.get(currNode)+1);
                    }
                }
            }
        }

        //check if there is a path
        public boolean hasPath(String end){
            return visited.get(end);
        }

        //look for path between start and end
        public ArrayList<String> getPath(String end){
            Stack<String> stack = new Stack<>();
            //search for path from from
            String temp = end;
            while(temp != null){
                stack.push(temp);
                temp = from.get(temp);
            }
            ArrayList<String> path = new ArrayList<>();
            while(!stack.empty()){
                path.add(stack.pop());
            }
            return path;
        }

        public void showPath(String end){
            if(hasPath(end)){
                ArrayList<String> path = getPath(end);
                System.out.println(path.toString());
            }else{
                System.out.println("no path exit");
            }
        }

        public int length(String end){
            return ord.get(end);
        }
    }

    private Boolean checkInput(Graph<String> graph, String input){
        if(input.length() == 4){
            if(graph.hasVertex(input)){
                return true;
            }else {
                Toast.makeText(this, "There is no such word in dictionary", Toast.LENGTH_SHORT).show();
                return false;
            }
        }else {
            Toast.makeText(this, "Please type word with 4 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void launchPlay(View view, ArrayList<String> path){
        Intent i = new Intent(this, PlayActivity.class);
        i.putExtra("Path", path);
        startActivity(i);
    }

    private String[] getRandomWords(ArrayList<String> list, Graph<String> graph) {
        Random random = new Random();
        int firstIndex, secondIndex;
        String start, end;
        ShortestPath shortestPath;

        do {
            // Randomly select indices until a valid pair is found
            firstIndex = random.nextInt(list.size());
            secondIndex = random.nextInt(list.size());
            start = list.get(firstIndex);
            end = list.get(secondIndex);

            // Check if there is a path between start and end, and the path length is less than 10
            shortestPath = new ShortestPath(graph, start);
        } while (!shortestPath.hasPath(end) || shortestPath.length(end) > 9 || start.equals(end));

        // Retrieve the strings at the selected indices
        String[] arr = {start, end};

        return arr;
    }
}