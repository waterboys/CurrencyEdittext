package com.umut.soysal.lib;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;

class CurrencyTextWatcher implements TextWatcher {

    public static int currentTextsize;
    protected static int cursorPosition = 0;
    protected static boolean okcommo = false;
    protected static boolean clickDot = false;
    protected static boolean isEmpty = false;
    protected static boolean clickDelete = false;
    protected static boolean rightPost = false;
    private final CurrencyEditText editText;
    private boolean ignoreIteration;
    private String lastGoodInput;

    private int latestTextLength = 0;
    private boolean isDeleteKeyActionCapturing = false;


    CurrencyTextWatcher(CurrencyEditText textBox) {
        editText = textBox;
        lastGoodInput = "";
        ignoreIteration = false;


        editText.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                isDeleteKeyActionCapturing = true;
                onKeyDelete();
            } else if (keyCode == 55) {
                clickDelete = false;
                okcommo = true;
                clickDot = true;
                cursorPosition = editText.getText().length() - 2;
                changeSignedKeyboard();
            } else if (keyCode == 56) {
                clickDelete = false;
                okcommo = true;
                clickDot = true;
                cursorPosition = editText.getText().length() - 2;
                changeSignedKeyboard();
            } else {
                clickDelete = false;
            }
            return false;
        });

    }

    private void onKeyDelete() {
        clickDelete = true;
    }

    private void detectDeleteKeyEvent(Editable editable) {
        if (isDeleteKeyActionCapturing) return; // Don'T use workaround.
        // https://issuetracker.google.com/issues/36964165
        // https://stackoverflow.com/a/19980975/11245490
        // TODO Workaround for now. It does not cover multiple selection delete.
        int length = editable.length();
        if (latestTextLength > length) onKeyDelete();
        latestTextLength = length;
    }

    @Override
    public void afterTextChanged(Editable editable) {

        //Use the ignoreIteration flag to stop our edits to the text field from triggering an endlessly recursive call to afterTextChanged
        if (!ignoreIteration) {
            ignoreIteration = true;
            detectDeleteKeyEvent(editable);
            //Start by converting the editable to something easier to work with, then remove all non-digit characters
            String newText = editable.toString();
            if (!isTextValid(newText)) { // Prevent text paste crash
                if (lastGoodInput != null) {
                    setRawValueFromLastGoodInput();
                    editText.setText(lastGoodInput);
                } else {
                    editText.setRawValue(0);
                }
                return;
            }

            String textToDisplay;
            if (newText.length() < 1) {
                lastGoodInput = "";
                editText.setRawValueInternal(0);
                editText.setText("");
                return;
            }

            if (clickDelete && okcommo && (editable.toString().length() - 2) <= editText.getSelectionStart()) {
                rightPost = true;
                if (editText.getSelectionStart() == editable.toString().length() - 1) {
                    newText = newText.substring(0, newText.length() - 1) + "0" + newText.substring(newText.length() - 1);
                } else if (editText.getSelectionStart() == editable.toString().length()) {
                    newText = newText + "0";
                }
            } else {
                rightPost = false;
            }

            if (!clickDelete && editText.getSelectionStart() - 1 >= 0) {
                String word = newText.substring(editText.getSelectionStart() - 1, editText.getSelectionStart());
                if (word.contentEquals(".") || word.contentEquals(",")) {
                    okcommo = true;
                    clickDot = true;
                } else {
                    okcommo = false;
                    clickDot = false;
                }
            } else {
                okcommo = false;
                clickDot = false;
            }

            newText = (editText.areNegativeValuesAllowed()) ? newText.replaceAll("[^0-9/-]", "") : newText.replaceAll("[^0-9]", "");

            boolean hasDecimal = !clickDelete && !okcommo && (editable.toString().length() - 2) <= editText.getSelectionStart();
            //ondalik bolumdesin
            if (hasDecimal) {
                newText = newText.substring(0, newText.length() - 1);
                rightPost = true;
            } else {
                rightPost = false;
            }

            try {
                textToDisplay = CurrencyTextFormatter.formatText(newText, editText.getLocale(), editText.getDefaultLocale(), editText.getDecimalDigits());
                textToDisplay = editText.removeCurrencySymbol(textToDisplay);
                boolean isAvailableToDisplay = textToDisplay.length() <= editText.getMaxDisplayTextLength();
                if (!isAvailableToDisplay) textToDisplay = lastGoodInput;
            } catch (IllegalArgumentException exception) {
                textToDisplay = lastGoodInput;
            }

            editText.setText(textToDisplay);
            //Store the last known good input so if there are any issues with new input later, we can fall back gracefully.
            lastGoodInput = textToDisplay;
            setRawValueFromLastGoodInput();

            if (!clickDelete && rightPost && cursorPosition <= (editable.toString().length() - 2)) {
                if (cursorPosition + 2 <= lastGoodInput.length()) {
                    editText.setSelection(cursorPosition + 1);
                } else {
                    editText.setSelection(lastGoodInput.length());
                }
                rightPost = false;
            } else if (!clickDelete && rightPost && cursorPosition == (editable.toString().length())) {
                editText.setSelection(lastGoodInput.length());
                rightPost = false;
            } else {
                if (isEmpty) {
                    editText.setSelection(1);
                    cursorPosition = (1);
                    isEmpty = false;
                } else if (okcommo) {
                    if (cursorPosition != lastGoodInput.length()) {
                        if (clickDot) {
                            editText.setSelection(editText.length() - 2);
                            clickDot = false;
                        } else {
                            editText.setSelection(cursorPosition + 1);
                        }
                    } else {
                        editText.setSelection(textToDisplay.length() - 1);
                    }
                } else {
                    okcommo = false;
                    int diff = Math.abs(currentTextsize - lastGoodInput.length());
                    if (clickDelete && !rightPost) {
                        if (diff == 2) {
                            if (cursorPosition == 0) {
                                editText.setSelection(0);
                            } else {
                                editText.setSelection(cursorPosition - 1);
                            }
                        } else if (diff > 2) {
                            editText.setSelection(0);
                        } else {
                            editText.setSelection(cursorPosition);
                        }
                        clickDelete = false;
                    } else if (clickDelete && rightPost) {
                        if (cursorPosition + 1 <= lastGoodInput.length()) {
                            editText.setSelection(cursorPosition - 1);
                        } else {
                            editText.setSelection(lastGoodInput.length() - 3);
                        }
                        clickDelete = false;
                    } else {
                        if ((cursorPosition + Math.abs(currentTextsize - lastGoodInput.length())) > lastGoodInput.length()) {
                            editText.setSelection(editText.getSelectionStart() + 1);
                        } else {

                            editText.setSelection(cursorPosition + Math.abs(currentTextsize - lastGoodInput.length()));
                        }

                    }
                }
            }

        } else {
            ignoreIteration = false;
            if (isEmpty && editable.toString().isEmpty()) {
                String tempText;
                try {
                    tempText = CurrencyTextFormatter.formatText("000", editText.getLocale(), editText.getDefaultLocale(), editText.getDecimalDigits());
                    tempText = tempText.substring(1);

                } catch (IllegalArgumentException exception) {
                    tempText = "";
                }
                editText.setText(tempText);
                editText.setSelection(1);
                cursorPosition = (1);
                isEmpty = false;
            }
        }
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        latestTextLength = s.length();
        if (s.length() == 0) {
            isEmpty = true;
        }
        if (start != 0) {
            currentTextsize = s.toString().length();
            cursorPosition = start;
            if (editText.getText().length() - 3 >= cursorPosition && editText.getText().length() - 3 > 0) {
                okcommo = false;
            } else {
                okcommo = true;
            }
        } else if (start == 0 && !ignoreIteration) {
            currentTextsize = s.toString().length();
            cursorPosition = start;
            if (editText.getText().length() - 3 >= cursorPosition && editText.getText().length() - 3 > 0) {
                okcommo = false;
            } else {
                okcommo = true;
            }
        }


    }

    @Override
    public void onTextChanged(final CharSequence s, int start, int before, int count) {
    }

    private void changeDecimalKeyboard() {
        editText.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
    }

    private void changeSignedKeyboard() {
        editText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
    }

    private void setRawValueFromLastGoodInput() {
        String rawText = lastGoodInput.replaceAll("[^0-9]", "");
        long rawValue = rawText.isEmpty() ? 0 : Long.parseLong(rawText);
        editText.setRawValueInternal(rawValue);
    }

    private boolean isTextValid(String text) {
        return text.replaceAll("[0-9,.]", "").isEmpty();
    }


}