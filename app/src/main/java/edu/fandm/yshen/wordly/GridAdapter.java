package edu.fandm.yshen.wordly;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.text.InputType;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class GridAdapter extends BaseAdapter {

    public interface WinCallback {
        void onWin();
    }

    private WinCallback winCallback;
    private Context context;
    private ArrayList<String> data;
    private ArrayList<String> texts;
    private boolean[] editTextFilledStatus; // Keep track of the filled status for each EditText

    public GridAdapter(Context context, ArrayList<String> data, WinCallback winCallback, ArrayList<String> currtexts) {
        this.context = context;
        this.data = data;
        this.winCallback = winCallback;
        this.texts = currtexts;

        // Initialize the editTextFilledStatus array with the size of the data list
        editTextFilledStatus = new boolean[data.size()];
        // Fill the array with initial values (assuming all EditTexts are initially empty)
        Arrays.fill(editTextFilledStatus, false);
    }


    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    // Method to get the EditText instance at a specific position
    public ArrayList<String> getEditTextValues() {
        return texts;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.grid_item_layout, parent, false);
        }

        EditText editTextItem = convertView.findViewById(R.id.editTextItem);

        if (position == 0 || position == getCount() - 1) {
            editTextItem.setText(data.get(position));
            editTextFilledStatus[position] = true;
        }else if(!texts.get(position).equals("")){
            editTextItem.setText(texts.get(position));
            editTextFilledStatus[position] = true;
        }

        // Set input type to "text" and specify the "Done" action
        editTextItem.setInputType(InputType.TYPE_CLASS_TEXT);
        editTextItem.setImeOptions(EditorInfo.IME_ACTION_DONE);

        // Set OnEditorActionListener for each EditText
        editTextItem.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    // Hide the keyboard
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editTextItem.getWindowToken(), 0);

                    // Perform your check when "Done" is clicked
                    String userInput = editTextItem.getText().toString();
                    String dataValue = data.get(position);

                    // Compare user input with the corresponding string in data
                    if (!userInput.equals(dataValue)) {
                        // If not equal, clear the text
                        editTextItem.setText("");
                        editTextFilledStatus[position] = false;
                    }else {
                        texts.add(position,userInput);
                        editTextFilledStatus[position] = true;
                    }

                    notifyDataSetChanged();

                    Log.d("GridAdapter", "Filled Status: " + Arrays.toString(editTextFilledStatus));

                    // Check if all EditText fields are filled
                    if (areAllEditTextsFilled()) {
                        // All fields are filled, perform your desired action
                        Log.d("GridAdapter", "win");
                        Toast.makeText(context, "You win!", Toast.LENGTH_SHORT).show();
                        winCallback.onWin();
                    }

                    return true; // Consume the event
                }
                return false; // Allow other listeners to be notified
            }
        });

        return convertView;
    }

    // Method to check if all EditText fields are filled
    private boolean areAllEditTextsFilled() {
        for (boolean filled : editTextFilledStatus) {
            if (!filled) {
                return false; // Return false if any EditText is not filled
            }
        }
        return true; // All EditTexts are filled
    }

    public void hint() {
        for (int i = 0; i < getCount(); i++) {
            if (!editTextFilledStatus[i]) {
                // Found an empty EditText
                if(i == 0){
                    Toast.makeText(context, "Hint: " + data.get(i), Toast.LENGTH_SHORT).show();
                }else {
                    String hint = compareString(data.get(i),data.get(i-1));
                    Toast.makeText(context, "Hint: " + hint, Toast.LENGTH_SHORT).show();
                }
                return; // Show hint for the first empty EditText and exit the method
            }
        }

        // If all EditText fields are filled, you can handle this case or show a different message
        Toast.makeText(context, "All fields are filled!", Toast.LENGTH_SHORT).show();
    }

    public String compareString(String str1, String str2){
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < str1.length(); i++){
            if(str1.charAt(i) != str2.charAt(i)){
                result.append(str1.charAt(i));
            }
        }
        return result.toString();
    }

}

