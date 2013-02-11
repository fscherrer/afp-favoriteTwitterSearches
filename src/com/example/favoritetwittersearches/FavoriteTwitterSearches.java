package com.example.favoritetwittersearches;

import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;

public class FavoriteTwitterSearches extends Activity {
  private SharedPreferences savedSearches;
  private TableLayout queryTableLayout;
  private EditText queryEditText;
  private EditText tagEditText;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    // obtém os objetos SharedPreferences que contém as pesquisas
    // salvas do usuário
    savedSearches = getSharedPreferences("searches", MODE_PRIVATE);

    // obtém uma referência para queryTableLayout
    queryTableLayout = (TableLayout) findViewById(R.id.queryTableLayout);

    // obtém referências para os dois componentes EditText e para o elemento
    // Button Save
    queryEditText = (EditText) findViewById(R.id.queryEditText);
    tagEditText = (EditText) findViewById(R.id.tagEditText);

    // registra receptores (listeners) para os componentes Button Save e Clear Tags
    Button saveButton = (Button) findViewById(R.id.saveButton);
    saveButton.setOnClickListener(saveButtonListener);
    Button clearTagsButton = (Button) findViewById(R.id.clearTagsButton);
    clearTagsButton.setOnClickListener(clearTagsButtonListener);

    // adiciona as pesquisas salvas anteriormente na interface gráfica do usuário
    refreshButtons(null);
  }

  // recria os componentes Button de tag de pesquisa e edição para todas as pesquisas salvas;
  // passa null para criar todos os componentes Button de tag e edição
  private void refreshButtons(String newTag) {
    // armazena tags salvas no array tags
    String[] tags = savedSearches.getAll().keySet().toArray(new String[0]);
    Arrays.sort(tags, String.CASE_INSENSITIVE_ORDER); // classifica por tag

    // se uma nova tag foi adicionada, insere na interface gráfica do usuário, no local apropriado
    if (newTag != null) {
      makeTagGUI(newTag, Arrays.binarySearch(tags, newTag));
    }
    // exibe a interface gráfica do usuário para todas as tags
    else {
      // exibe todas as pesquisas salvas
      for (int index = 0; index < tags.length; ++index)
        makeTagGUI(tags[index], index);
    }
  }

  // adiciona nova pesquisa no arquivo de salvamento e, então, atualiza todos
  // os componentes Button
  private void makeTag(String query, String tag) {
    // originalQuery vai ser null se estivermos modificando uma pesquisa
    // já existente
    String originalQuery = savedSearches.getString(tag, null);

    // obtém um SharedPreferences.Editor para armazenar o novo par tag/consulta
    SharedPreferences.Editor preferencesEditor = savedSearches.edit();
    preferencesEditor.putString(tag, query); // armazena a pesquisa atual
    preferencesEditor.apply(); // armazena as preferências atualizadas

    // se essa é uma nova consulta, adiciona sua interface gráfica
    if (originalQuery == null)
      refreshButtons(tag); // adiciona um novo bot;ao para essa tag
  }

  // adiciona um novo botão tag e o botão de edição correspondente na interface
  // gráfica do usuário
  private void makeTagGUI(String tag, int index) {
    // obtém uma referência para o serviço LayoutInflater
    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

    // infla new_tag_view.xml para criar novos componentes Button de tag e edição
    View newTagView = inflater.inflate(R.layout.new_tag_view, null);

    // obtém newTagButton, configura seu texto e registra seu receptor
    Button newTagButton = (Button) newTagView.findViewById(R.id.newTagButton);
    newTagButton.setText(tag);
    newTagButton.setOnClickListener(queryButtonListener);

    // obtém newEditButton e registra seu receptor
    Button newEditButton = (Button) newTagView.findViewById(R.id.newEditButton);
    newEditButton.setOnClickListener(editButtonListener);

    // adiciona novos botões de tag e edição no componente queryTableLayout
    queryTableLayout.addView(newTagView, index);
  }

  // remove do aplicativo todos os componentes Button de pesquisa salva
  private void clearButtons() {
    // remove todos os componentes Button de pesquisa salva
    queryTableLayout.removeAllViews();
  }

  // cria um novo componente Button e o adiciona no elemento ScrollView
  public OnClickListener saveButtonListener = new OnClickListener() {

    @Override
    public void onClick(View v) {
      // cria a tag se queryEditText e tagEditText não estão vazios
      if (queryEditText.getText().length() > 0 && tagEditText.getText().length() > 0) {
        makeTag(queryEditText.getText().toString(), tagEditText.getText().toString());
        queryEditText.setText(""); // limpa queryEditText
        tagEditText.setText(""); // limpa tagEditText

        // oculta o teclado virtual
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).
            hideSoftInputFromWindow(tagEditText.getWindowToken(), 0);
      }
      // exibe uma mensagem pedindo para que o usuário forneça uma consulta e uma tag
      else{
        // cria um novo AlertDialog Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(FavoriteTwitterSearches.this);
        
        builder.setTitle(R.string.missingTitle); // string da barra de título
        
        // fornece um botão OK que simplesmente fecha a caixa de diálogo
        builder.setPositiveButton(R.string.OK, null);
        
        // configura a mensagem para apareer
        builder.setMessage(R.string.missingMessage);
        
        // cria AlertDialog a partir de AlertDialog.Builder
        AlertDialog errorDialog = builder.create();
        errorDialog.show(); // exibe a caixa de diálogo
      }
    }
  };
  
  // apaga todas as pesquisas salvas
  public OnClickListener clearTagsButtonListener = new OnClickListener() {
    
    @Override
    public void onClick(View v) {
      // cria um novo AlertDialog Builder
      AlertDialog.Builder builder = new AlertDialog.Builder(FavoriteTwitterSearches.this);
      
      builder.setTitle(R.string.confirmTitle); // string da barra de título
      
      // fornece um botão OK que simplesmente fecha a caixa de diálogo
      builder.setPositiveButton(R.string.erase, new DialogInterface.OnClickListener() {
        
        @Override
        public void onClick(DialogInterface dialog, int which) {
          clearButtons(); // apaga todas as pesquisas salvas
          
          // obtém um elemento SharedPreferences.Editor para limpar pesquisas
          SharedPreferences.Editor preferencesEditor = savedSearches.edit();
          
          // remove todos os pares tag/consulta
          preferencesEditor.clear();
          
          // efetiva as alterações
          preferencesEditor.apply();
        }
      });
      
      builder.setCancelable(true);
      builder.setNegativeButton(R.string.cancel, null);
      
      // configura a mensagem a ser exibida
      builder.setMessage(R.string.confirmMessage);
      
      // cria AlertDialog a partir de AlertDialog.Builder
      AlertDialog confirmDialog = builder.create();
      confirmDialog.show(); // exibe o objeto Dialog
    }
  };
  
  // carrega a pesquisa seleciona em um navegador Web
  public OnClickListener queryButtonListener = new OnClickListener() {
    
    @Override
    public void onClick(View v) {
      // obtém a consulta
      String buttonText = ((Button)v).getText().toString();
      String query = savedSearches.getString(buttonText, null);
      
      // cria a URL correspondente à consulta do componente Button tocado
      String urlString = getString(R.string.searchURL) + query;
      
      // cria um objet Intent para ativar um navegador Web
      Intent getURL = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
      
      startActivity(getURL);
    }
  };
  
  // edita a pesquisa selecionada
  public OnClickListener editButtonListener = new OnClickListener() {
    
    @Override
    public void onClick(View v) {
      // obtém todos os componentes necessários da interface gráfica do usuário
      TableRow buttonTableRow = (TableRow)v.getParent();
      Button searchButton = (Button)buttonTableRow.findViewById(R.id.newTagButton);
      
      String tag = searchButton.getText().toString();
      
      // configura os componentes EditText para corresponder à tag e à consulta escolhidas
      tagEditText.setText(tag);
      queryEditText.setText(savedSearches.getString(tag, null));
    }
  };
}
