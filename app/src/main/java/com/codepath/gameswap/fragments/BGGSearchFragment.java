package com.codepath.gameswap.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.codepath.gameswap.BGGAsyncTask;
import com.codepath.gameswap.BGGGameAdapter;
import com.codepath.gameswap.EndlessRecyclerViewScrollListener;
import com.codepath.gameswap.R;
import com.codepath.gameswap.models.BGGGame;

import org.jetbrains.annotations.NotNull;
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

    private List<BGGGame> games;
    private LinearLayoutManager layoutManager;
    private BGGGameAdapter adapter;
    private RecyclerView rvResults;
    private SwipeRefreshLayout swipeContainer;
    private EndlessRecyclerViewScrollListener scrollListener;

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

        rvResults = view.findViewById(R.id.rvResults);
        swipeContainer = view.findViewById(R.id.swipeContainer);

        layoutManager = new LinearLayoutManager(context);
        games = new ArrayList<>();

        adapter = new BGGGameAdapter(context, games);

        rvResults.setAdapter(adapter);
        rvResults.setLayoutManager(layoutManager);

        setHasOptionsMenu(true);
/*
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                queryPosts(true);
            }
        };
        // Adds the scroll listener to RecyclerView
        rvPosts.addOnScrollListener(scrollListener);

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryPosts(false);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);*/
    }

    /*private void queryPosts(final boolean loadNext) {
        // Specify which class to query
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // Find all posts
        query.include(Post.KEY_USER);
        query.setLimit(20);
        query.addDescendingOrder(Post.KEY_CREATED_AT);
        if (loadNext) {
            Date olderThanDate = allPosts.get(allPosts.size()-1).getCreatedAt();
            query.whereLessThan(Post.KEY_CREATED_AT, olderThanDate);
        }
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                if (!loadNext) {
                    adapter.clear();
                    scrollListener.resetState();
                    swipeContainer.setRefreshing(false);
                }
                adapter.addAll(posts);
                adapter.notifyDataSetChanged();
            }
        });
    }*/

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search_bar, menu);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setIconified(false);
        searchView.setMaxWidth( Integer.MAX_VALUE );
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryString) {
                // perform query here
                querySearch(queryString);
                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                // see https://code.google.com/p/android/issues/detail?id=24599
                searchView.clearFocus();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String queryString) {
                //querySearch(queryString);
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void querySearch(String queryString) {
        adapter.clear();
        String modifiedQuery = queryString.replaceAll("\\s", "+");
        String base = "https://www.boardgamegeek.com/xmlapi2/search?query=%s&type=boardgame";
        String url = String.format(base, modifiedQuery);
        BGGAsyncTask test = new BGGAsyncTask(this, BGGResponseType.SEARCH);
        test.execute(url);
    }

    private void getSearchResults(Document doc) {
        NodeList itemList = doc.getElementsByTagName("item");
        int maxResults = Math.min(itemList.getLength(), 20);
        for (int i = 0; i < maxResults; i++) {
            Node itemNode = itemList.item(i);
            if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                Element item = (Element) itemNode;
                String id = item.getAttribute("id");
                Log.d(TAG, "Getting details of: " + id);
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
        String ageRating = "2";
        for (int i = 0; i < ageNodes.getLength(); i++) {
            Node ageNode = ageNodes.item(i);
            if (ageNode.getNodeType() == Node.ELEMENT_NODE) {
                Element ageElement = (Element) ageNode;
                ageRating = ageElement.getAttribute("value");
                break;
            }
        }

        BGGGame game = new BGGGame(id, name, imageUrl, difficulty, ageRating);
        adapter.add(game);
    }
}