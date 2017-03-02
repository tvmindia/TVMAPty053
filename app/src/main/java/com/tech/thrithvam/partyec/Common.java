package com.tech.thrithvam.partyec;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

class Common {

    void NavigationBarItemClick(Context context, MenuItem item){
        int id = item.getItemId();

        if (id == R.id.nav_shop_by_category) {
            Intent intent=new Intent(context,CategoryList.class);
            context.startActivity(intent);
        }/* else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }*/

    }

    void SearchViewActionBarInitialisation(final Context context, Menu menu){
        final SearchView searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(context, searchView.getQuery().toString(), Toast.LENGTH_SHORT).show();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    void LoadImage(Context context,ImageView imageView, String imageURL, int failImage){
        if(!imageURL.equals("null")){
            Glide.with(context)
                    .load(imageURL)//adapterContext.getResources().getString(R.string.url) +imageURL.substring(imageURL.indexOf("img")))
                    .fitCenter()
                    .thumbnail(0.1f)
                    .error(failImage)
                    .into(imageView);
        }
        else{
            Glide.with(context)
                    .load(failImage)
                    .fitCenter()
                    .into(imageView);
        }
    }
}
