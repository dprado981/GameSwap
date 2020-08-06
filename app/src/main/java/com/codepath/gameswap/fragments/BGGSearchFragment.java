package com.codepath.gameswap.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.codepath.gameswap.BGGAsyncTask;
import com.codepath.gameswap.BGGGameAdapter;
import com.codepath.gameswap.R;
import com.codepath.gameswap.models.BGGGame;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class BGGSearchFragment extends Fragment implements BGGAsyncTask.BGGResponse {

    public static final String TAG = PostsFragment.class.getSimpleName();

    private Context context;
    private String lastSearch;

    private List<BGGGame> games;
    private LinearLayoutManager layoutManager;
    private BGGGameAdapter adapter;
    private RecyclerView rvResults;
    private SwipeRefreshLayout swipeContainer;

    public BGGSearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bgg_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getContext();

        final TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.bgg_lookup);
        final android.widget.SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setMaxWidth(Integer.MAX_VALUE);

        int searchIconId = ((LinearLayout)searchView.getChildAt(0)).getChildAt(1).getId();
        ImageView searchIcon = searchView.findViewById(searchIconId);
        searchIcon.setColorFilter(android.R.color.white);

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    tvTitle.setVisibility(View.GONE);
                } else {
                    tvTitle.setVisibility(View.VISIBLE);
                }
            }
        });
        searchView.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchQuery) {
                querySearch(searchQuery.trim());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        searchView.setOnCloseListener(new android.widget.SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                tvTitle.setVisibility(View.VISIBLE);
                querySearch(null);
                return false;
            }
        });

        searchView.setIconified(false);

        rvResults = view.findViewById(R.id.rvResults);
        swipeContainer = view.findViewById(R.id.swipeContainer);

        layoutManager = new LinearLayoutManager(context);
        games = new ArrayList<>();

        adapter = new BGGGameAdapter(context, games);

        rvResults.setAdapter(adapter);
        rvResults.setLayoutManager(layoutManager);

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeContainer.setRefreshing(true);
                querySearch(lastSearch);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(
                R.color.colorAccent,
                R.color.colorPrimary,
                R.color.colorDelete);
    }


    private void querySearch(String queryString) {
        adapter.clear();
        games.clear();
        if (queryString == null || queryString.isEmpty()) {
            return;
        }
        String modifiedQuery = queryString.replaceAll("\\s", "+");
        lastSearch = modifiedQuery;
        String base = "https://www.boardgamegeek.com/xmlapi2/search?query=%s&type=boardgame";
        String url = String.format(base, modifiedQuery);
        BGGAsyncTask test = new BGGAsyncTask(this, BGGResponseType.SEARCH);
        test.execute(url);
    }

    private void getSearchResults(Document doc) {
        NodeList itemList = doc.getElementsByTagName("item");
        int maxResults = Math.min(itemList.getLength(), 25);
        for (int i = 0; i < maxResults; i++) {
            Node itemNode = itemList.item(i);
            if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                Element item = (Element) itemNode;
                String id = item.getAttribute("id");
                queryDetails(id);
            }
        }
    }

    private void queryDetails(String id) {
        String base = "https://www.boardgamegeek.com/xmlapi2/thing?id=%s&stats=1";
        String url = String.format(base, id);
        BGGAsyncTask test = new BGGAsyncTask(this, BGGResponseType.DETAIL);
        test.execute(url);
    }

    @Override
    public void onFinish(String output, BGGResponseType responseType) {
        swipeContainer.setRefreshing(false);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(output));
            Document doc = builder.parse(is);
            if (responseType == BGGResponseType.SEARCH) {
                getSearchResults(doc);
            } else if (responseType == BGGResponseType.DETAIL) {
                getDetails(doc);
            }

        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
    }

    private void getDetails(Document doc) {
        // Get id of game
        NodeList itemNodes = doc.getElementsByTagName("item");
        String id = null;
        for (int i = 0; i < itemNodes.getLength(); i++) {
            Node itemNode = itemNodes.item(i);
            if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                Element itemElement = (Element) itemNode;
                id = itemElement.getAttribute("id");
                break;
            }
        }

        // Get image URL of game
        NodeList imageNodes = doc.getElementsByTagName("image");
        String imageUrl = null;
        for (int i = 0; i < imageNodes.getLength(); i++) {
            Node imageNode = imageNodes.item(i);
            if (imageNode.getNodeType() == Node.ELEMENT_NODE) {
                Element imageElement = (Element) imageNode;
                imageUrl = imageElement.getTextContent();
                break;
            }
        }

        // Get primary name of game
        NodeList nameNodes = doc.getElementsByTagName("name");
        String name = null;
        for (int i = 0; i < nameNodes.getLength(); i++) {
            Node nameNode = nameNodes.item(i);
            if (nameNode.getNodeType() == Node.ELEMENT_NODE) {
                Element nameElement = (Element) nameNode;
                if (nameElement.getAttribute("type").equals("primary")) {
                    name = nameElement.getAttribute("value");
                    break;
                }
            }
        }

        // Get difficulty of game ("weight")
        NodeList weightNodes = doc.getElementsByTagName("averageweight");
        float difficulty = 0;
        for (int i = 0; i < weightNodes.getLength(); i++) {
            Node weightNode = weightNodes.item(i);
            if (weightNode.getNodeType() == Node.ELEMENT_NODE) {
                Element weightElement = (Element) weightNode;
                difficulty = Float.parseFloat(weightElement.getAttribute("value"));
                break;
            }
        }

        // Get age rating of game ("minage")
        NodeList ageNodes = doc.getElementsByTagName("minage");
        String ageRating = "2+";
        for (int i = 0; i < ageNodes.getLength(); i++) {
            Node ageNode = ageNodes.item(i);
            if (ageNode.getNodeType() == Node.ELEMENT_NODE) {
                Element ageElement = (Element) ageNode;
                ageRating = ageElement.getAttribute("value") + "+";
                break;
            }
        }

        // Get min players time of game
        NodeList minPlayersNodes = doc.getElementsByTagName("minplayers");
        int minPlayers = 0;
        for (int i = 0; i < minPlayersNodes.getLength(); i++) {
            Node minPlayersNode = minPlayersNodes.item(i);
            if (minPlayersNode.getNodeType() == Node.ELEMENT_NODE) {
                Element minPlayersElement = (Element) minPlayersNode;
                minPlayers = Integer.parseInt(minPlayersElement.getAttribute("value"));
                break;
            }
        }

        // Get max players time of game
        NodeList maxPlayersNodes = doc.getElementsByTagName("maxplayers");
        int maxPlayers = 0;
        for (int i = 0; i < maxPlayersNodes.getLength(); i++) {
            Node maxPlayersNode = maxPlayersNodes.item(i);
            if (maxPlayersNode.getNodeType() == Node.ELEMENT_NODE) {
                Element maxPlayersElement = (Element) maxPlayersNode;
                maxPlayers = Integer.parseInt(maxPlayersElement.getAttribute("value"));
                break;
            }
        }

        // Get min playing time of game
        NodeList minPlayingTimeNodes = doc.getElementsByTagName("minplaytime");
        int minPlayingTime = 0;
        for (int i = 0; i < minPlayingTimeNodes.getLength(); i++) {
            Node minPlayingTimeNode = minPlayingTimeNodes.item(i);
            if (minPlayingTimeNode.getNodeType() == Node.ELEMENT_NODE) {
                Element minPlayingTimeElement = (Element) minPlayingTimeNode;
                minPlayingTime = Integer.parseInt(minPlayingTimeElement.getAttribute("value"));
                break;
            }
        }

        // Get max playing time of game
        NodeList maxPlayingTimeNodes = doc.getElementsByTagName("maxplaytime");
        int maxPlayingTime = 0;
        for (int i = 0; i < maxPlayingTimeNodes.getLength(); i++) {
            Node maxPlayingTimeNode = maxPlayingTimeNodes.item(i);
            if (maxPlayingTimeNode.getNodeType() == Node.ELEMENT_NODE) {
                Element maxPlayingTimeElement = (Element) maxPlayingTimeNode;
                maxPlayingTime = Integer.parseInt(maxPlayingTimeElement.getAttribute("value"));
                break;
            }
        }

        BGGGame game = new BGGGame(id, name, imageUrl, difficulty, ageRating,
                minPlayers, maxPlayers, minPlayingTime, maxPlayingTime);
        adapter.add(game);
    }
}