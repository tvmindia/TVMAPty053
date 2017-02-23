package com.tech.thrithvam.partyec;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import android.widget.Toast;

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
}
