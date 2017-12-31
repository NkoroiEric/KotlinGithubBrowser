package com.scaleup.kotlingithubbrowser.util

import com.scaleup.kotlingithubbrowser.vo.Contributor
import com.scaleup.kotlingithubbrowser.vo.Repo
import com.scaleup.kotlingithubbrowser.vo.User

object TestUtil {

    fun createUser(login : String): User {
        return User(login, null,
                login + " name ", null,null, null)
    }

    fun createRepos(count : Int, owner : String, name : String, description : String) : List<Repo>{
        val repos = mutableListOf<Repo>()
        for (i in 0 until count){
            repos.add(createRepo(owner + i, name + i, description + i))
        }
        return repos
    }

    fun createRepo(owner: String, name: String, description: String): Repo {
        return createRepo(Repo.UNKNOWN_ID, owner, name, description)
    }

    fun createRepo(id: Int, owner: String, name: String, description: String): Repo{
        return Repo(id, name, owner + "/" + name,
                description, Repo.Owner(owner, null),3)
    }

    fun createContributor(repo: Repo, login : String, contribution : Int): Contributor {
        val contributor = Contributor(login, contribution, null)
        contributor.repoName = repo.name
        contributor.repoOwner = repo.owner.login
        return contributor
    }
}