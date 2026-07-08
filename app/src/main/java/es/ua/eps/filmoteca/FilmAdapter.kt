package es.ua.eps.filmoteca

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class FilmAdapter(
    context: Context,
    private val films: List<Film>
) : ArrayAdapter<Film>(context, 0, films) {

    private class ViewHolder(v: View) {
        val img: ImageView = v.findViewById(R.id.imgPoster)
        val title: TextView = v.findViewById(R.id.tvTitle)
        val director: TextView = v.findViewById(R.id.tvDirector)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_film, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val film = films[position]
        holder.img.setImageResource(if (film.imageResId != 0) film.imageResId else R.mipmap.ic_launcher)
        holder.title.text = film.title ?: ""
        holder.director.text = film.director ?: ""

        return view
    }
}
