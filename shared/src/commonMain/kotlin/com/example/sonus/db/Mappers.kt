package com.example.sonus.db

import com.example.sonus.network.*

fun SongDTO.toEntity() = SongEntity(
    id = id,
    title = title,
    artist = artist,
    duration = duration,
    coverPath = coverPath,
    blurHash = blurHash,
    filePath = filePath,
    albumId = albumId,
    isFavorite = isFavorite,
    isInPlaylist = isInPlaylist
)

fun SongEntity.toDTO() = SongDTO(
    id = id,
    title = title,
    artist = artist,
    duration = duration,
    coverPath = coverPath,
    blurHash = blurHash,
    filePath = filePath,
    albumId = albumId,
    isFavorite = isFavorite,
    isInPlaylist = isInPlaylist
)

fun AlbumDTO.toEntity() = AlbumEntity(
    id = id ?: -1L,
    title = title,
    artist = artist,
    coverPath = coverPath,
    blurHash = blurHash,
    isSaved = isSaved
)

fun AlbumEntity.toDTO() = AlbumDTO(
    id = id,
    title = title,
    artist = artist,
    coverPath = coverPath,
    blurHash = blurHash,
    isSaved = isSaved
)

fun PlaylistDTO.toEntity() = PlaylistEntity(
    id = id ?: -1L,
    name = name,
    description = description,
    songCount = songCount
)

fun PlaylistEntity.toDTO() = PlaylistDTO(
    id = id,
    name = name,
    description = description,
    songCount = songCount
)
